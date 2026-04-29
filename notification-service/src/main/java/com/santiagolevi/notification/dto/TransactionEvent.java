package com.santiagolevi.notification.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TransactionEvent {
    private String eventType;
    private UUID transactionId;
    private String transactionType;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String actor;
    private String previousState;
    private String newState;
    private Instant occurredAt;
    private String correlationId;
}
