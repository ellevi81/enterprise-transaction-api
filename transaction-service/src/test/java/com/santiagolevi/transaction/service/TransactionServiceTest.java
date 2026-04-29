package com.santiagolevi.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santiagolevi.transaction.dto.TransactionRequest;
import com.santiagolevi.transaction.dto.TransactionResponse;
import com.santiagolevi.transaction.dto.TransactionUpdateRequest;
import com.santiagolevi.transaction.exception.TransactionNotFoundException;
import com.santiagolevi.transaction.model.Transaction;
import com.santiagolevi.transaction.model.TransactionStatus;
import com.santiagolevi.transaction.model.TransactionType;
import com.santiagolevi.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository repository;
    @Mock RabbitTemplate rabbitTemplate;
    @Mock ObjectMapper objectMapper;

    @InjectMocks TransactionService service;

    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = Transaction.builder()
            .id(UUID.randomUUID())
            .type(TransactionType.EXPENSE)
            .amount(new BigDecimal("150.00"))
            .currency("USD")
            .description("Test transaction")
            .status(TransactionStatus.PENDING)
            .createdBy("user@test.com")
            .build();
    }

    @Test
    void create_savesAndPublishesEvent() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.EXPENSE);
        req.setAmount(new BigDecimal("150.00"));
        req.setCurrency("usd");
        req.setDescription("Test");

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        TransactionResponse response = service.create(req, "user@test.com");

        assertThat(response.getId()).isEqualTo(sampleTransaction.getId());
        verify(repository).save(any(Transaction.class));
        verify(rabbitTemplate).convertAndSend(eq("transaction.events"), contains("transaction."), any(Object.class));
    }

    @Test
    void findById_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
            .isInstanceOf(TransactionNotFoundException.class)
            .hasMessageContaining(id.toString());
    }

    @Test
    void update_changesDescriptionAndStatus() throws Exception {
        UUID id = sampleTransaction.getId();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.findById(id)).thenReturn(Optional.of(sampleTransaction));
        when(repository.save(any())).thenReturn(sampleTransaction);

        TransactionUpdateRequest req = new TransactionUpdateRequest();
        req.setDescription("Updated desc");
        req.setStatus(TransactionStatus.COMPLETED);

        TransactionResponse response = service.update(id, req, "admin");

        verify(repository).save(any(Transaction.class));
        verify(rabbitTemplate).convertAndSend(eq("transaction.events"), contains("transaction."), any(Object.class));
    }

    @Test
    void update_rejectsCancelledStatusChange() {
        sampleTransaction.setStatus(TransactionStatus.CANCELLED);
        UUID id = sampleTransaction.getId();
        when(repository.findById(id)).thenReturn(Optional.of(sampleTransaction));

        TransactionUpdateRequest req = new TransactionUpdateRequest();
        req.setStatus(TransactionStatus.PENDING);

        assertThatThrownBy(() -> service.update(id, req, "user"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("CANCELLED");
    }

    @Test
    void delete_removesAndPublishesEvent() throws Exception {
        UUID id = sampleTransaction.getId();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.findById(id)).thenReturn(Optional.of(sampleTransaction));

        service.delete(id, "admin");

        verify(repository).delete(sampleTransaction);
        verify(rabbitTemplate).convertAndSend(eq("transaction.events"), contains("transaction."), any(Object.class));
    }

    @Test
    void publishEvent_failurDoesNotPropagateException() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.INCOME);
        req.setAmount(new BigDecimal("10.00"));
        req.setCurrency("EUR");

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.save(any())).thenReturn(sampleTransaction);
        doThrow(new RuntimeException("RabbitMQ down"))
            .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Must not throw — event failure is swallowed
        TransactionResponse response = service.create(req, "user");
        assertThat(response).isNotNull();
    }
}
