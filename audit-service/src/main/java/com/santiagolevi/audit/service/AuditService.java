package com.santiagolevi.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.santiagolevi.audit.dto.TransactionEvent;
import com.santiagolevi.audit.model.AuditEntry;
import com.santiagolevi.audit.repository.AuditEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEntryRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AuditEntry record(TransactionEvent event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            payload = "{}";
        }

        AuditEntry entry = AuditEntry.builder()
            .eventType(event.getEventType())
            .entityType("TRANSACTION")
            .entityId(event.getTransactionId() != null ? event.getTransactionId().toString() : "unknown")
            .actor(event.getActor() != null ? event.getActor() : "system")
            .timestamp(event.getOccurredAt() != null ? event.getOccurredAt() : Instant.now())
            .payload(payload)
            .sourceService("transaction-service")
            .build();

        AuditEntry saved = repository.save(entry);
        log.info("Audit entry recorded: id={} eventType={} entityId={}", saved.getId(), saved.getEventType(), saved.getEntityId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<AuditEntry> findByActor(String actor, Pageable pageable) {
        return repository.findByActorOrderByTimestampDesc(actor, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditEntry> findByEntity(String entityType, String entityId, Pageable pageable) {
        return repository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditEntry> search(String eventType, String entityType, String actor, Pageable pageable) {
        return repository.search(eventType, entityType, actor, pageable);
    }
}
