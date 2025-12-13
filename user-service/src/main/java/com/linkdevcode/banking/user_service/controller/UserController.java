package com.linkdevcode.banking.user_service.controller;

import com.linkdevcode.banking.user_service.exception.ResourceNotFoundException;
import com.linkdevcode.banking.user_service.model.request.BalanceUpdateRequest;
import com.linkdevcode.banking.user_service.model.request.ChangePasswordRequest;
import com.linkdevcode.banking.user_service.model.request.ResetPasswordRequest;
import com.linkdevcode.banking.user_service.model.request.UserLoginRequest;
import com.linkdevcode.banking.user_service.model.request.UserRegisterRequest;
import com.linkdevcode.banking.user_service.model.response.JwtResponse;
import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.service.JwtBlacklistService;
import com.linkdevcode.banking.user_service.service.JwtTokenProvider;
import com.linkdevcode.banking.user_service.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
@Tag(name = "User Management", description = "APIs for user registration, authentication, search and security flows.")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtBlacklistService jwtBlacklistService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(
        UserService userService,
        JwtBlacklistService jwtBlacklistService,
        JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtBlacklistService = jwtBlacklistService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // Register Endpoint
    @Operation(summary = "Register a new user and initialize their bank account")
    @PostMapping("register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        // The service layer handles creating both the User and the associated Account entity
        UserResponse registeredUser = userService.registerUser(request);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    // Login Endpoint (Authentication)
    @Operation(summary = "Authenticate user and generate JWT token")
    @PostMapping("login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody UserLoginRequest request) {
        // Authenticates credentials and returns the JWT token
        var jwtResponse = userService.authenticateUser(request);
        return ResponseEntity.ok(jwtResponse);
    }

    // Logout Endpoint
    @Operation(summary = "Logout user and invalidate JWT token")
    @PostMapping("logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // 1. Parse token to get remaining expiration time (Required for Redis TTL)
            Jws<Claims> claims = jwtTokenProvider.validateJwtToken(token);
            Date expiration = claims.getBody().getExpiration();
            long expirationSeconds = (expiration.getTime() - Instant.now().toEpochMilli()) / 1000;

            // 2. Add to blacklist with calculated TTL
            if (expirationSeconds > 0) {
                 jwtBlacklistService.blacklistToken(token, expirationSeconds);
            }
        }
        return ResponseEntity.ok().build();
    }
    
    // Change Password Endpoint (Authenticated User)
    @Operation(summary = "Change password for an authenticated user")
    @PostMapping("change-password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader(name = "X-User-ID", required = true) Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        userService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }
    
    // Forgot Password Endpoint
    @Operation(summary = "Initiate password reset: generate token and send email")
    @PostMapping("forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam("email") String email) {
        // Wrap with try-catch or handle error internally, but always return 200 OK 
        // to prevent user enumeration attacks.
        try {
            userService.createPasswordResetToken(email);
        } catch (ResourceNotFoundException e) {
            // Log error, but return OK to obfuscate whether the email exists
            log.warn("Attempted password reset for non-existent email: {}", email);
        }
        return ResponseEntity.ok().build();
    }

    // Reset Password Endpoint
    @Operation(summary = "Final step of password reset: set new password using token")
    @PostMapping("reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    // API to get the current account balance of the authenticated user.
    @Operation(summary = "Get current balance of the authenticated user (External)")
    @GetMapping("{id}/balance/get")
    public ResponseEntity<BigDecimal> getMyAccountBalance(@PathVariable Long id) {
        
        BigDecimal balance = userService.getBalance(id);
        return ResponseEntity.ok(balance);
    }

    // API to deduct an amount from a user's account balance.
    @Operation(summary = "Deduct balance from user account (Internal)")
    @PostMapping("{id}/balance/deduct")
    public ResponseEntity<Void> deductBalance(@PathVariable Long id,
                                              @RequestBody BalanceUpdateRequest request) {
        userService.deductBalance(id, request.getAmount());
        return ResponseEntity.ok().build();
    }

    // API to add an amount to a user's account balance.
    @Operation(summary = "Add balance to user account (Internal)")
    @PostMapping("{id}/balance/add")
    public ResponseEntity<Void> addBalance(@PathVariable Long id,
                                           @RequestBody BalanceUpdateRequest request) {
        userService.addBalance(id, request.getAmount());
        return ResponseEntity.ok().build();
    }

    // API to fetch user profile details (e.g., full name) for enrichment.
    @Operation(summary = "Get user profile details for enrichment")
    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        // Implementation will call userService to fetch basic user details (like full name)
        // For now, this is a placeholder. You'll need a simple UserProfileInternalDTO.
        return ResponseEntity.ok("Placeholder profile data for user: " + id);
    }

    // Search User Endpoint
    @Operation(summary = "Search users with pagination and filtering by full name")
    @GetMapping("search")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        
        // Searches for users (useful for recipient lookup before transfer)
        Page<UserResponse> result = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(result);
    }
}