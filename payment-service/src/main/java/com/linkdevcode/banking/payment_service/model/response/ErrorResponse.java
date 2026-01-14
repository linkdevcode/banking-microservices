package com.linkdevcode.banking.payment_service.model.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * Standard error response model.
 */
@Data
public class ErrorResponse {
    
    // Timestamp of when the error occurred.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // HTTP status code of the error.
    private int status;

    // Error type or reason.
    private String error;
    
    // Detailed error message.
    private String message;

    // Path of the request that caused the error.
    private String path;
}