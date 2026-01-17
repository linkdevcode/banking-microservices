package com.linkdevcode.banking.voucher_service.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linkdevcode.banking.voucher_service.service.VoucherService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/voucher")
@RequiredArgsConstructor
public class InternalVoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "Generate daily top transfer vouchers")
    @PostMapping("/generate")
    public ResponseEntity<Void> generate(
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate targetDate
    ) {
        voucherService.generateDailyVouchers(targetDate);
        return ResponseEntity.ok().build();
    }
}