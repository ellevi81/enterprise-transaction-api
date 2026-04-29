package com.santiagolevi.audit.controller;

import com.santiagolevi.audit.model.AuditEntry;
import com.santiagolevi.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditEntry>> search(
        @RequestParam(required = false) String eventType,
        @RequestParam(required = false) String entityType,
        @RequestParam(required = false) String actor,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.search(eventType, entityType, actor, pageable));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditEntry>> byEntity(
        @PathVariable String entityType,
        @PathVariable String entityId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.findByEntity(entityType, entityId, pageable));
    }

    @GetMapping("/actor/{actor}")
    public ResponseEntity<Page<AuditEntry>> byActor(
        @PathVariable String actor,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.findByActor(actor, pageable));
    }
}
