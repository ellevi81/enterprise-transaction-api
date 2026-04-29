package com.santiagolevi.transaction.repository;

import com.santiagolevi.transaction.model.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionItemRepository extends JpaRepository<TransactionItem, UUID> {
    List<TransactionItem> findByTransactionId(UUID transactionId);
    void deleteByTransactionId(UUID transactionId);
}
