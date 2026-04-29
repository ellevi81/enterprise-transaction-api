package com.santiagolevi.transaction.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransactionItemRequest {

    @NotNull(message = "inventoryItemId is required")
    private UUID inventoryItemId;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.0001", message = "unitPrice must be positive")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal unitPrice;
}
