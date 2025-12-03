package com.linkdevcode.banking.payment_service.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Request DTO for initiating a money transfer.
 */
@Data
public class TransferRequest {
    // ID of the user receiving the funds
    @NotNull
    private Long recipientId;

    // Amount to be transferred
    @NotNull
    @Min(value = 1, message = "Amount must be greater than zero")
    private BigDecimal amount;

    // Optional message for the transaction
    private String message;
}