package com.santiagolevi.audit.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Entity
@Immutable
@Table(name = "audit_entries")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq")
    @SequenceGenerator(name = "audit_seq", sequenceName = "audit_entries_id_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 100)
    private String entityType;

    @Column(nullable = false, length = 255)
    private String entityId;

    @Column(nullable = false, length = 255)
    private String actor;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(columnDefinition = "text")
    private String payload;

    @Column(length = 100)
    private String sourceService;
}
