package com.linkdevcode.banking.voucher_service.repository.specification;

import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

import com.linkdevcode.banking.voucher_service.entity.Voucher;
import com.linkdevcode.banking.voucher_service.enumeration.EVoucherStatus;
import com.linkdevcode.banking.voucher_service.enumeration.EVoucherType;

public class VoucherSpecifications {
    
    public static Specification<Voucher> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null : 
            cb.equal(root.get("userId"), userId);
    }

    public static Specification<Voucher> hasVoucherType(EVoucherType transactionType) {
        return (root, query, cb) -> transactionType == null ? null : 
            cb.equal(root.get("transactionType"), transactionType);
    }

    public static Specification<Voucher> hasVoucherStatus(EVoucherStatus transactionStatus) {
        return (root, query, cb) -> transactionStatus == null ? null : 
            cb.equal(root.get("transactionStatus"), transactionStatus);
    }

    public static Specification<Voucher> isValidAt(LocalDateTime targetTime) {
        return (root, query, cb) -> {
            if (targetTime == null) return null;

            return cb.and(
                cb.lessThanOrEqualTo(root.get("issuedAt"), targetTime),
                cb.greaterThanOrEqualTo(root.get("expiredAt"), targetTime)
            );
        };
    }
}
