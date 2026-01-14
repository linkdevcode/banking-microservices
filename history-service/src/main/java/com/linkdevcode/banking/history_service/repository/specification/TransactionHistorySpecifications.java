package com.linkdevcode.banking.history_service.repository.specification;

import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

import com.linkdevcode.banking.history_service.entity.TransactionHistory;
import com.linkdevcode.banking.history_service.enumeration.ETransactionStatus;
import com.linkdevcode.banking.history_service.enumeration.ETransactionType;

public class TransactionHistorySpecifications {
    
    public static Specification<TransactionHistory> hasUserId(Long userId) {
        return (root, query, cb) -> userId == null ? null : 
            cb.or(
                cb.equal(root.get("fromUserId"), userId),
                cb.equal(root.get("toUserId"), userId)
            );
    }

    public static Specification<TransactionHistory> hasAccountNumber(String accountNum) {
        return (root, query, cb) -> accountNum == null ? null : 
            cb.or(
                cb.equal(root.get("fromAccountNumber"), accountNum),
                cb.equal(root.get("toAccountNumber"), accountNum)
            );
    }

    public static Specification<TransactionHistory> hasTransactionType(ETransactionType transactionType) {
        return (root, query, cb) -> transactionType == null ? null : 
            cb.equal(root.get("transactionType"), transactionType);
    }

    public static Specification<TransactionHistory> hasTransactionStatus(ETransactionStatus transactionStatus) {
        return (root, query, cb) -> transactionStatus == null ? null : 
            cb.equal(root.get("status"), transactionStatus);
    }

    public static Specification<TransactionHistory> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to == null) return cb.greaterThanOrEqualTo(root.get("transactionTime"), from);
            if (from == null && to != null) return cb.lessThanOrEqualTo(root.get("transactionTime"), to);
            return cb.between(root.get("transactionTime"), from, to);
        };
    }
}
