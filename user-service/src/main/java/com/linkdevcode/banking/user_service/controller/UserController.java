package com.linkdevcode.banking.user_service.controller;

import com.linkdevcode.banking.user_service.model.request.UserSearchRequest;
import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "APIs for user profile management and searching users.")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get my profile")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
        @Parameter(hidden = true)
        @RequestHeader(value = "X-User-Id", required = false) Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @Operation(summary = "Search users (Admin)")
    @PostMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestBody UserSearchRequest request) {

        return ResponseEntity.ok(userService.searchUsers(request));
    }
}