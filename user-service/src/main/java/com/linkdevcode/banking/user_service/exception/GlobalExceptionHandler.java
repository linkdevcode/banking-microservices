package com.linkdevcode.banking.user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.linkdevcode.banking.user_service.model.response.ErrorResponse;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handles validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                details,
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handles entity not found exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(createErrorResponse(
                HttpStatus.NOT_FOUND, 
                "RESOURCE_NOT_FOUND", 
                ex.getMessage(), 
                request.getDescription(false)), HttpStatus.NOT_FOUND);
    }

    // Handles insufficient balance exceptions
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex, WebRequest request) {
        return new ResponseEntity<>(createErrorResponse(
                HttpStatus.BAD_REQUEST, 
                "INSUFFICIENT_BALANCE", 
                ex.getMessage(), 
                request.getDescription(false)), HttpStatus.BAD_REQUEST);
    }

    // Handles invalid credentials and token exceptions
    @ExceptionHandler({InvalidCredentialsException.class, InvalidTokenException.class})
    public ResponseEntity<ErrorResponse> handleSecurityExceptions(RuntimeException ex, WebRequest request) {
        String errorCode = (ex instanceof InvalidTokenException) ? "INVALID_TOKEN" : "INVALID_CREDENTIALS";
        return new ResponseEntity<>(createErrorResponse(
                HttpStatus.BAD_REQUEST, 
                errorCode, 
                ex.getMessage(), 
                request.getDescription(false)), HttpStatus.BAD_REQUEST);
    }

    // Handles illegal arguments and states
    @ExceptionHandler({
        IllegalArgumentException.class, 
        IllegalStateException.class,
        EntityNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleLogicExceptions(RuntimeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (ex instanceof EntityNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof IllegalStateException) {
            status = HttpStatus.CONFLICT; 
        }

        ErrorResponse errorResponse = createErrorResponse(
                status,
                (ex instanceof IllegalArgumentException) ? "INVALID_INPUT" : "INVALID_STATE",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    // Handles all other runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleGeneralRuntime(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "INTERNAL_SERVER_ERROR", 
                "An unexpected error occurred", 
                request.getDescription(false)), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Utility method to create standardized error responses
    private ErrorResponse createErrorResponse(HttpStatus status, String error, String message, String path) {
        ErrorResponse response = new ErrorResponse();
        response.setTimestamp(LocalDateTime.now());
        response.setStatus(status.value());
        response.setError(error);
        response.setMessage(message);
        response.setPath(path.replace("uri=", ""));
        return response;
    }
}