package com.santiagolevi.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santiagolevi.audit.dto.TransactionEvent;
import com.santiagolevi.audit.model.AuditEntry;
import com.santiagolevi.audit.repository.AuditEntryRepository;
import com.santiagolevi.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock AuditEntryRepository repository;
    @Mock ObjectMapper objectMapper;
    @InjectMocks AuditService auditService;

    @Test
    void record_persistsAuditEntry() throws Exception {
        TransactionEvent event = new TransactionEvent();
        event.setEventType("TRANSACTION_CREATED");
        event.setTransactionId(UUID.randomUUID());
        event.setActor("user@test.com");
        event.setOccurredAt(Instant.now());

        AuditEntry saved = AuditEntry.builder()
            .id(1L)
            .eventType("TRANSACTION_CREATED")
            .entityType("TRANSACTION")
            .entityId(event.getTransactionId().toString())
            .actor("user@test.com")
            .timestamp(Instant.now())
            .payload("{}")
            .sourceService("transaction-service")
            .build();

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.save(any())).thenReturn(saved);

        AuditEntry result = auditService.record(event);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEventType()).isEqualTo("TRANSACTION_CREATED");
        verify(repository).save(any(AuditEntry.class));
    }

    @Test
    void record_usesNowWhenOccurredAtIsNull() throws Exception {
        TransactionEvent event = new TransactionEvent();
        event.setEventType("TRANSACTION_DELETED");
        event.setTransactionId(UUID.randomUUID());
        event.setActor("admin");
        event.setOccurredAt(null);

        AuditEntry saved = AuditEntry.builder()
            .id(2L)
            .eventType("TRANSACTION_DELETED")
            .entityType("TRANSACTION")
            .entityId(event.getTransactionId().toString())
            .actor("admin")
            .timestamp(Instant.now())
            .payload("{}")
            .sourceService("transaction-service")
            .build();

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.save(any())).thenReturn(saved);

        AuditEntry result = auditService.record(event);
        assertThat(result).isNotNull();
    }
}
