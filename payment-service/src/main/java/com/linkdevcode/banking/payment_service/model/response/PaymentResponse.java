package com.linkdevcode.banking.payment_service.model.response;

import lombok.Data;

import com.linkdevcode.banking.payment_service.enumeration.ETransactionStatus;

import lombok.AllArgsConstructor;

/**
 * Response DTO for money transfer status.
 */
@Data
@AllArgsConstructor
public class PaymentResponse {
    
    // Status of the transaction
    private ETransactionStatus status;

    // ID of the transaction
    private Long transactionId;

    // Optional message about the transfer
    private String message;
}