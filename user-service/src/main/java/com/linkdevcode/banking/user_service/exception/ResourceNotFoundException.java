package com.linkdevcode.banking.user_service.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

/**
 * Custom exception indicating that the requested resource was not found.
 * Maps to HTTP Status 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}