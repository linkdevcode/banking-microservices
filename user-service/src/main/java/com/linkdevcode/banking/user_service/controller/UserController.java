package com.linkdevcode.banking.user_service.controller;

import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get user profile")
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getMyProfile(
            @RequestHeader("X-User-Id") Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @Operation(summary = "Search users")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        return ResponseEntity.ok(userService.searchUsers(query, pageable));
    }
}