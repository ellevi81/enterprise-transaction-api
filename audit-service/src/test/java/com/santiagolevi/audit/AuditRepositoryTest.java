package com.santiagolevi.audit;

import com.santiagolevi.audit.model.AuditEntry;
import com.santiagolevi.audit.repository.AuditEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AuditRepositoryTest {

    @Autowired
    private AuditEntryRepository repository;

    private AuditEntry buildEntry(String eventType, String entityType, String entityId, String actor) {
        return AuditEntry.builder()
            .eventType(eventType)
            .entityType(entityType)
            .entityId(entityId)
            .actor(actor)
            .timestamp(Instant.now())
            .payload("{}")
            .sourceService("test-service")
            .build();
    }

    @Test
    void save_persistsEntry() {
        AuditEntry entry = buildEntry("ITEM_CREATED", "ITEM", "item-1", "user@test.com");
        AuditEntry saved = repository.save(entry);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEventType()).isEqualTo("ITEM_CREATED");
    }

    @Test
    void findByActor_returnsMatchingEntries() {
        repository.save(buildEntry("EV1", "ITEM", "1", "alice@test.com"));
        repository.save(buildEntry("EV2", "ITEM", "2", "alice@test.com"));
        repository.save(buildEntry("EV3", "ITEM", "3", "bob@test.com"));

        Page<AuditEntry> result = repository.findByActorOrderByTimestampDesc(
            "alice@test.com", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(e -> e.getActor().equals("alice@test.com"));
    }

    @Test
    void findByEntityTypeAndEntityId_returnsMatchingEntries() {
        repository.save(buildEntry("CREATED", "ITEM", "item-99", "user1"));
        repository.save(buildEntry("UPDATED", "ITEM", "item-99", "user2"));
        repository.save(buildEntry("CREATED", "ORDER", "order-1", "user1"));

        Page<AuditEntry> result = repository.findByEntityTypeAndEntityIdOrderByTimestampDesc(
            "ITEM", "item-99", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(e -> e.getEntityId().equals("item-99"));
    }

    @Test
    void findByEventType_filtersCorrectly() {
        repository.save(buildEntry("ITEM_CREATED", "ITEM", "1", "user"));
        repository.save(buildEntry("ITEM_DELETED", "ITEM", "2", "user"));
        repository.save(buildEntry("ITEM_CREATED", "ITEM", "3", "user"));

        Page<AuditEntry> result = repository.findByEventTypeOrderByTimestampDesc(
            "ITEM_CREATED", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void search_withNullFilters_returnsAll() {
        repository.save(buildEntry("EV1", "TYPE1", "id1", "user1"));
        repository.save(buildEntry("EV2", "TYPE2", "id2", "user2"));

        Page<AuditEntry> result = repository.search(null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void countByEntityTypeAndEntityId_returnsCorrectCount() {
        repository.save(buildEntry("EV1", "PRODUCT", "prod-1", "user"));
        repository.save(buildEntry("EV2", "PRODUCT", "prod-1", "user"));
        repository.save(buildEntry("EV3", "PRODUCT", "prod-2", "user"));

        long count = repository.countByEntityTypeAndEntityId("PRODUCT", "prod-1");
        assertThat(count).isEqualTo(2);
    }

    @Test
    void savedEntry_cannotBeModified() {
        assertThat(AuditEntry.class.getAnnotation(org.hibernate.annotations.Immutable.class))
            .isNotNull();
    }
}
