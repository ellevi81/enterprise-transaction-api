package com.santiagolevi.transaction.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transaction_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "inventory_item_id", nullable = false)
    private UUID inventoryItemId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;
}
