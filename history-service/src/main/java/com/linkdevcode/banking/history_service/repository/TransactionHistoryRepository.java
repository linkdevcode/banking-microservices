package com.linkdevcode.banking.history_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.linkdevcode.banking.history_service.entity.TransactionHistory;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long>,
    JpaSpecificationExecutor<TransactionHistory> {
    
    boolean existsByTransactionId(Long transactionId);
}
