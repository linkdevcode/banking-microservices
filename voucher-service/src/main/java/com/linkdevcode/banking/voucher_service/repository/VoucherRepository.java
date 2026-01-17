package com.linkdevcode.banking.voucher_service.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.linkdevcode.banking.voucher_service.entity.Voucher;
import com.linkdevcode.banking.voucher_service.enumeration.EVoucherType;

public interface VoucherRepository extends JpaRepository<Voucher, Long>,
    JpaSpecificationExecutor<Voucher> {

    int countByUserIdAndTypeAndIssuedAtBetween(
        Long userId,
        EVoucherType type,
        LocalDateTime start,
        LocalDateTime end
    );

    List<Voucher> findByUserId(Long userId);
}
