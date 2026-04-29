package com.santiagolevi.transaction.dto;

import com.santiagolevi.transaction.model.TransactionStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransactionUpdateRequest {

    @Size(max = 1000)
    private String description;

    private TransactionStatus status;
}
