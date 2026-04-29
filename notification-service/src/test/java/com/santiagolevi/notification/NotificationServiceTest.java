package com.santiagolevi.notification;

import com.santiagolevi.notification.dto.TransactionEvent;
import com.santiagolevi.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks NotificationService notificationService;

    private TransactionEvent event(String type) {
        TransactionEvent e = new TransactionEvent();
        e.setEventType(type);
        e.setTransactionId(UUID.randomUUID());
        e.setTransactionType("DEBIT");
        e.setAmount(new BigDecimal("100.00"));
        e.setCurrency("USD");
        e.setStatus("PENDING");
        e.setActor("user@test.com");
        e.setOccurredAt(Instant.now());
        return e;
    }

    @Test
    void notify_createdEvent_doesNotThrow() {
        assertThatCode(() -> notificationService.notify(event("TRANSACTION_CREATED")))
            .doesNotThrowAnyException();
    }

    @Test
    void notify_updatedEvent_doesNotThrow() {
        assertThatCode(() -> notificationService.notify(event("TRANSACTION_UPDATED")))
            .doesNotThrowAnyException();
    }

    @Test
    void notify_deletedEvent_doesNotThrow() {
        assertThatCode(() -> notificationService.notify(event("TRANSACTION_DELETED")))
            .doesNotThrowAnyException();
    }

    @Test
    void notify_unknownEvent_doesNotThrow() {
        assertThatCode(() -> notificationService.notify(event("UNKNOWN_EVENT")))
            .doesNotThrowAnyException();
    }
}
