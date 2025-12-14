package com.linkdevcode.banking.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/internal/users")
@Tag(name = "Internal - User")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get basic user profile for enrichment")
    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getInternalProfile(@PathVariable Long id) {

        return ResponseEntity.ok(userService.getUserProfile(id));
    }
}