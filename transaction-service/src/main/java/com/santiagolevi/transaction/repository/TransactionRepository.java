package com.santiagolevi.transaction.repository;

import com.santiagolevi.transaction.model.Transaction;
import com.santiagolevi.transaction.model.TransactionStatus;
import com.santiagolevi.transaction.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByCreatedBy(String createdBy, Pageable pageable);

    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    Page<Transaction> findByType(TransactionType type, Pageable pageable);

    @Query("""
        SELECT t FROM Transaction t
        WHERE (:type IS NULL OR t.type = :type)
          AND (:status IS NULL OR t.status = :status)
          AND (:createdBy IS NULL OR t.createdBy = :createdBy)
        ORDER BY t.createdAt DESC
    """)
    Page<Transaction> search(
        @Param("type") TransactionType type,
        @Param("status") TransactionStatus status,
        @Param("createdBy") String createdBy,
        Pageable pageable
    );
}
