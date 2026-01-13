package com.linkdevcode.banking.payment_service.model.response;

import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * Response DTO for money transfer status.
 */
@Data
@AllArgsConstructor
public class PaymentResponse {
    
    // Status of the transfer
    private String status;

    // ID of the transaction
    private Long transactionId;

    // Optional message about the transfer
    private String message;
}