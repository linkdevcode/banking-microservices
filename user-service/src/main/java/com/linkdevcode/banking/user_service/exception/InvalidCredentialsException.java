package com.linkdevcode.banking.user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception indicating invalid username/password or incorrect old password.
 * Maps to HTTP Status 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}