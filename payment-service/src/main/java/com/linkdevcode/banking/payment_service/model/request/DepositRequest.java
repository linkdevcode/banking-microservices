package com.linkdevcode.banking.payment_service.model.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepositRequest {
    
    // Account number where the deposit will be made
    @NotNull
    private String toAccountNumber;

    // ID of the user receiving the funds
    @NotNull
    @Min(value = 1, message = "Amount must be greater than zero")
    private BigDecimal amount;

    // Optional message for the transaction
    private String message;
}
