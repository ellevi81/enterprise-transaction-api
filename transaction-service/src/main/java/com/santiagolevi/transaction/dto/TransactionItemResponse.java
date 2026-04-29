package com.santiagolevi.transaction.dto;

import com.santiagolevi.transaction.model.TransactionItem;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class TransactionItemResponse {
    UUID id;
    UUID inventoryItemId;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal subtotal;

    public static TransactionItemResponse from(TransactionItem item) {
        return TransactionItemResponse.builder()
            .id(item.getId())
            .inventoryItemId(item.getInventoryItemId())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .build();
    }
}
