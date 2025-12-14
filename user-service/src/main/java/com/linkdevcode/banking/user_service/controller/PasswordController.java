package com.linkdevcode.banking.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.user_service.exception.ResourceNotFoundException;
import com.linkdevcode.banking.user_service.model.request.ChangePasswordRequest;
import com.linkdevcode.banking.user_service.model.request.ResetPasswordRequest;
import com.linkdevcode.banking.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/password")
@Tag(name = "Password & Security")
@Slf4j
public class PasswordController {

    private final UserService userService;

    public PasswordController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Change password (authenticated user)")
    @PostMapping("/change")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("X-User-ID") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Forgot password")
    @PostMapping("/forgot")
    public ResponseEntity<Void> forgotPassword(
            @RequestParam String email) {

        try {
            userService.createPasswordResetToken(email);
        } catch (ResourceNotFoundException e) {
            log.warn("Password reset requested for non-existent email: {}", email);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reset password using token")
    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        userService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}