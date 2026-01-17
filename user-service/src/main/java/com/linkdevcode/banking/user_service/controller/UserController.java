package com.linkdevcode.banking.user_service.controller;

import com.linkdevcode.banking.user_service.model.response.UserResponse;
import com.linkdevcode.banking.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "APIs for user profile management and voucher searching")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get my profile")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
        @Parameter(hidden = true)
        @RequestHeader(value = "X-User-Id", required = false) Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }
}