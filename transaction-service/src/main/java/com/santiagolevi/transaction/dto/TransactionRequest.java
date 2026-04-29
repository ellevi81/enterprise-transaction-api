package com.santiagolevi.transaction.dto;

import com.santiagolevi.transaction.model.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TransactionRequest {

    @NotNull(message = "type is required")
    private TransactionType type;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0001", message = "amount must be positive")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
    private String currency;

    @Size(max = 1000, message = "description max length is 1000")
    private String description;

    @Valid
    private List<TransactionItemRequest> items;
}
