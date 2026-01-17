package com.linkdevcode.banking.voucher_service.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.voucher_service.model.request.AdminVoucherSearchRequest;
import com.linkdevcode.banking.voucher_service.model.response.AdminVoucherSearchResponse;
import com.linkdevcode.banking.voucher_service.service.VoucherService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/voucher")
@RequiredArgsConstructor
@Tag(name = "Admin Voucher", description = "APIs for admins to view all users' voucher.")
public class AdminVoucherController {
    
    private final VoucherService voucherService;
    
    // Search vouchers for admin
    @PostMapping("/search")
    public Page<AdminVoucherSearchResponse> searchAllVoucher(
        @RequestBody AdminVoucherSearchRequest request
    ){
        return voucherService.searchForAdmin(request);
    }
}
