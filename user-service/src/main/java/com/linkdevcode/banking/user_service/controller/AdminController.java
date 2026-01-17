package com.linkdevcode.banking.user_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.user_service.model.request.UserSearchRequest;
import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "APIs for admin role")
public class AdminController {

    private final UserService userService;

    @Operation(summary = "Search users")
    @PostMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestBody UserSearchRequest request) {

        return ResponseEntity.ok(userService.searchUsers(request));
    }
}