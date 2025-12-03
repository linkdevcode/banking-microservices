package com.linkdevcode.banking.user_service.controller;

import com.linkdevcode.banking.user_service.model.request.UserLoginRequest;
import com.linkdevcode.banking.user_service.model.request.UserRegisterRequest;
import com.linkdevcode.banking.user_service.model.response.JwtResponse;
import com.linkdevcode.banking.user_service.model.response.UserResponse;
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
@RequestMapping("/user")
@Tag(name = "User Management", description = "APIs for user registration, authentication, and search.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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