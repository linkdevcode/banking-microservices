package com.linkdevcode.banking.voucher_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.voucher_service.model.request.UserVoucherSearchRequest;
import com.linkdevcode.banking.voucher_service.model.response.UserVoucherSearchResponse;
import com.linkdevcode.banking.voucher_service.service.VoucherService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/voucher")
@RequiredArgsConstructor
@Tag(name = "User Voucher", description = "APIs for users to view their voucher")
public class UserVoucherController {
    
    private final VoucherService voucherService;

    // Search voucher for user
    @PostMapping("/search")
    public Page<UserVoucherSearchResponse> searchMyHistory(
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody UserVoucherSearchRequest request
    ){
        return voucherService.searchForUser(userId, request);
    }
}
