package com.linkdevcode.banking.user_service.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

/**
 * Custom exception indicating that a provided token (e.g., reset token) is invalid, expired, or not found.
 * Maps to HTTP Status 400 Bad Request to signify a client error in providing the token.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException(String message) {
        super(message);
    }

    // Constructor with both message and cause (optional)
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}