package com.santiagolevi.transaction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.santiagolevi.transaction.dto.*;
import com.santiagolevi.transaction.event.TransactionEvent;
import com.santiagolevi.transaction.exception.TransactionNotFoundException;
import com.santiagolevi.transaction.model.*;
import com.santiagolevi.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public TransactionResponse create(TransactionRequest request, String actor) {
        Transaction tx = Transaction.builder()
            .type(request.getType())
            .amount(request.getAmount())
            .currency(request.getCurrency().toUpperCase())
            .description(request.getDescription())
            .status(TransactionStatus.PENDING)
            .createdBy(actor)
            .build();

        if (request.getItems() != null) {
            List<TransactionItem> items = request.getItems().stream()
                .map(itemReq -> TransactionItem.builder()
                    .transaction(tx)
                    .inventoryItemId(itemReq.getInventoryItemId())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .build())
                .collect(Collectors.toList());
            tx.getItems().addAll(items);
        }

        Transaction saved = repository.save(tx);
        log.info("Transaction created id={} type={} amount={} actor={}", saved.getId(), saved.getType(), saved.getAmount(), actor);

        publishEvent("TRANSACTION_CREATED", saved, null, actor);
        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(UUID id) {
        return TransactionResponse.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAll(TransactionType type, TransactionStatus status, String createdBy, Pageable pageable) {
        return repository.search(type, status, createdBy, pageable)
            .map(TransactionResponse::from);
    }

    @Transactional
    public TransactionResponse update(UUID id, TransactionUpdateRequest request, String actor) {
        Transaction tx = getOrThrow(id);
        String previousState = toJson(tx);

        if (request.getDescription() != null) {
            tx.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            validateStatusTransition(tx.getStatus(), request.getStatus());
            tx.setStatus(request.getStatus());
        }

        Transaction saved = repository.save(tx);
        log.info("Transaction updated id={} status={} actor={}", id, saved.getStatus(), actor);

        publishEvent("TRANSACTION_UPDATED", saved, previousState, actor);
        return TransactionResponse.from(saved);
    }

    @Transactional
    public void delete(UUID id, String actor) {
        Transaction tx = getOrThrow(id);
        String previousState = toJson(tx);
        repository.delete(tx);
        log.info("Transaction deleted id={} actor={}", id, actor);

        publishEvent("TRANSACTION_DELETED", tx, previousState, actor);
    }

    private Transaction getOrThrow(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + id));
    }

    private void validateStatusTransition(TransactionStatus current, TransactionStatus next) {
        if (current == TransactionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a CANCELLED transaction.");
        }
        if (current == TransactionStatus.COMPLETED && next == TransactionStatus.PENDING) {
            throw new IllegalStateException("Cannot revert a COMPLETED transaction to PENDING.");
        }
    }

    private void publishEvent(String eventType, Transaction tx, String previousState, String actor) {
        try {
            TransactionEvent event = TransactionEvent.builder()
                .eventType(eventType)
                .transactionId(tx.getId())
                .transactionType(tx.getType() != null ? tx.getType().name() : null)
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .status(tx.getStatus() != null ? tx.getStatus().name() : null)
                .actor(actor)
                .previousState(previousState)
                .newState(toJson(tx))
                .occurredAt(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .build();

            rabbitTemplate.convertAndSend(
                "transaction.events",
                "transaction." + eventType.toLowerCase(),
                event
            );
        } catch (Exception e) {
            // Event publishing failure must not roll back the business transaction.
            log.error("Failed to publish event {} for transaction {}: {}", eventType, tx.getId(), e.getMessage());
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
