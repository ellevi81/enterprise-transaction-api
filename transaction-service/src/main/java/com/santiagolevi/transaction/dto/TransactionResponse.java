package com.santiagolevi.transaction.dto;

import com.santiagolevi.transaction.model.Transaction;
import com.santiagolevi.transaction.model.TransactionStatus;
import com.santiagolevi.transaction.model.TransactionType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Value
@Builder
public class TransactionResponse {
    UUID id;
    TransactionType type;
    BigDecimal amount;
    String currency;
    String description;
    TransactionStatus status;
    String createdBy;
    List<TransactionItemResponse> items;
    Instant createdAt;
    Instant updatedAt;

    public static TransactionResponse from(Transaction tx) {
        return TransactionResponse.builder()
            .id(tx.getId())
            .type(tx.getType())
            .amount(tx.getAmount())
            .currency(tx.getCurrency())
            .description(tx.getDescription())
            .status(tx.getStatus())
            .createdBy(tx.getCreatedBy())
            .items(tx.getItems() == null ? List.of() :
                tx.getItems().stream().map(TransactionItemResponse::from).collect(Collectors.toList()))
            .createdAt(tx.getCreatedAt())
            .updatedAt(tx.getUpdatedAt())
            .build();
    }
}
