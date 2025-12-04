package com.linkdevcode.banking.user_service.model.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Standard error response model.
 */
@Data
public class ErrorResponse {
    
    // Timestamp of when the error occurred.
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