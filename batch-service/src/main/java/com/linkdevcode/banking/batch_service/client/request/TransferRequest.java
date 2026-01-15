package com.linkdevcode.banking.batch_service.client.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    
    // Account number sending the funds
    @NotNull
    private String fromAccountNumber;

    // Account number from which the transfer will be made
    @NotNull
    private String toAccountNumber;

    // Amount to be transferred
    @NotNull
    @Min(value = 1, message = "Amount must be greater than zero")
    private BigDecimal amount;

    // Optional message for the transaction
    private String message;
}
