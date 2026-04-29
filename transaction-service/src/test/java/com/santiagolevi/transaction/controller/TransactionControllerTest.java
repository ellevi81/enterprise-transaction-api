package com.santiagolevi.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santiagolevi.transaction.dto.TransactionRequest;
import com.santiagolevi.transaction.dto.TransactionResponse;
import com.santiagolevi.transaction.exception.GlobalExceptionHandler;
import com.santiagolevi.transaction.exception.TransactionNotFoundException;
import com.santiagolevi.transaction.model.TransactionStatus;
import com.santiagolevi.transaction.model.TransactionType;
import com.santiagolevi.transaction.service.JwtService;
import com.santiagolevi.transaction.service.TransactionService;
import com.santiagolevi.transaction.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TransactionService service;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private TransactionResponse sampleResponse() {
        return TransactionResponse.builder()
            .id(UUID.randomUUID())
            .type(TransactionType.EXPENSE)
            .amount(new BigDecimal("200.00"))
            .currency("USD")
            .status(TransactionStatus.PENDING)
            .createdBy("user@test.com")
            .createdAt(Instant.now())
            .build();
    }

    @Test
    @WithMockUser
    void create_returns201() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setType(TransactionType.EXPENSE);
        req.setAmount(new BigDecimal("200.00"));
        req.setCurrency("USD");

        TransactionResponse resp = sampleResponse();
        when(service.create(any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    @WithMockUser
    void getById_returns200() throws Exception {
        TransactionResponse resp = sampleResponse();
        UUID id = resp.getId();
        when(service.findById(id)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/transactions/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser
    void getById_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenThrow(new TransactionNotFoundException("Transaction not found: " + id));

        mockMvc.perform(get("/api/v1/transactions/{id}", id))
            .andExpect(status().isNotFound());
    }

    @Test
    void getById_returns401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/{id}", UUID.randomUUID()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void create_returns400OnMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void delete_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/transactions/{id}", id).with(csrf()))
            .andExpect(status().isNoContent());
    }
}
