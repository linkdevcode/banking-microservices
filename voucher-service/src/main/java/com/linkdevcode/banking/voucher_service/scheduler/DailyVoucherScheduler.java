package com.linkdevcode.banking.voucher_service.scheduler;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.linkdevcode.banking.voucher_service.service.VoucherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyVoucherScheduler {

    private final VoucherService voucherService;

    /**
     * Run at 00:05 every day
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void runDailyTopTransferJob() {

        LocalDate targetDate = LocalDate.now().minusDays(1);

        log.info("[VOUCHER-JOB] Start generating vouchers for date={}", targetDate);

        voucherService.generateDailyVouchers(targetDate);

        log.info("[VOUCHER-JOB] Finished generating vouchers for date={}", targetDate);
    }
}