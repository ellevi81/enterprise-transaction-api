package com.santiagolevi.transaction.controller;

import com.santiagolevi.transaction.dto.*;
import com.santiagolevi.transaction.model.TransactionStatus;
import com.santiagolevi.transaction.model.TransactionType;
import com.santiagolevi.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
        @Valid @RequestBody TransactionRequest request,
        @AuthenticationPrincipal UserDetails principal
    ) {
        TransactionResponse response = service.create(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> list(
        @RequestParam(required = false) TransactionType type,
        @RequestParam(required = false) TransactionStatus status,
        @RequestParam(required = false) String createdBy,
        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(type, status, createdBy, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody TransactionUpdateRequest request,
        @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.ok(service.update(id, request, principal.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserDetails principal
    ) {
        service.delete(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}
