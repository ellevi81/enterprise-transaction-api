package com.santiagolevi.audit.repository;

import com.santiagolevi.audit.model.AuditEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {

    Page<AuditEntry> findByActorOrderByTimestampDesc(String actor, Pageable pageable);

    Page<AuditEntry> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId, Pageable pageable);

    Page<AuditEntry> findByEventTypeOrderByTimestampDesc(String eventType, Pageable pageable);

    @Query("SELECT a FROM AuditEntry a WHERE " +
           "(:eventType IS NULL OR a.eventType = :eventType) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:actor IS NULL OR a.actor = :actor) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditEntry> search(
        @Param("eventType") String eventType,
        @Param("entityType") String entityType,
        @Param("actor") String actor,
        Pageable pageable
    );

    long countByEntityTypeAndEntityId(String entityType, String entityId);
}
