package com.linkdevcode.banking.user_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.user_service.model.request.UserLoginRequest;
import com.linkdevcode.banking.user_service.model.request.UserRegisterRequest;
import com.linkdevcode.banking.user_service.model.response.JwtResponse;
import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.security.JwtTokenProvider;
import com.linkdevcode.banking.user_service.service.JwtBlacklistService;
import com.linkdevcode.banking.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtBlacklistService jwtBlacklistService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService,
                          JwtBlacklistService jwtBlacklistService,
                          JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtBlacklistService = jwtBlacklistService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegisterRequest request) {

        UserResponse user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "Login and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody UserLoginRequest request) {

        return ResponseEntity.ok(userService.authenticateUser(request));
    }

    @Operation(summary = "Logout and invalidate JWT")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            var claims = jwtTokenProvider.validateJwtToken(token);
            var expiration = claims.getBody().getExpiration();
            long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;

            if (ttl > 0) {
                jwtBlacklistService.blacklistToken(token, ttl);
            }
        }
        return ResponseEntity.ok().build();
    }
}
