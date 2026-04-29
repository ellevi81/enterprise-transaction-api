package com.santiagolevi.transaction.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    private String eventType;       // TRANSACTION_CREATED, TRANSACTION_UPDATED, TRANSACTION_DELETED
    private UUID transactionId;
    private String transactionType; // INCOME, EXPENSE, TRANSFER
    private BigDecimal amount;
    private String currency;
    private String status;
    private String actor;
    private String previousState;   // JSON snapshot before change
    private String newState;        // JSON snapshot after change
    private Instant occurredAt;
    private String correlationId;
}
