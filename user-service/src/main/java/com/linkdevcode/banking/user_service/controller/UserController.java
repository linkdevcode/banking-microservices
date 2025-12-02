package com.linkdevcode.banking.user_service.controller;

import com.linkdevcode.banking.user_service.dto.JwtResponse;
import com.linkdevcode.banking.user_service.dto.UserLoginRequest;
import com.linkdevcode.banking.user_service.dto.UserRegisterRequest;
import com.linkdevcode.banking.user_service.dto.UserResponse;
import com.linkdevcode.banking.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Management", description = "APIs for user registration, authentication, and search.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Task 2.2: Register Endpoint
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse registeredUser = userService.registerUser(request);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    // Task 2.3: Login Endpoint (Authentication)
    @Operation(summary = "Authenticate user and generate JWT token")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody UserLoginRequest request) {
        // Placeholder: The actual response will be a JWT token string
        var jwtResponse = userService.authenticateUser(request);
        return ResponseEntity.ok(jwtResponse);
    }

    // Task 2.4: Search User Endpoint (Requires Auth - ROLE_ADMIN/ROLE_USER access control should be added later)
    @Operation(summary = "Search users with pagination and filtering by full name")
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<UserResponse> result = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(result);
    }

    // Placeholder for other user related endpoints (e.g., reset password, get profile)
}