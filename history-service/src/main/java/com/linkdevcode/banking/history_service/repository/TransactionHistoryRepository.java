package com.linkdevcode.banking.history_service.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.linkdevcode.banking.history_service.entity.TransactionHistory;
import com.linkdevcode.banking.history_service.enumeration.ETransactionStatus;
import com.linkdevcode.banking.history_service.repository.projection.TopUserProjection;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long>,
    JpaSpecificationExecutor<TransactionHistory> {
    
    boolean existsByTransactionId(Long transactionId);

    @Query("""
        SELECT 
            t.fromUserId AS userId,
            SUM(t.amount) AS totalAmount
        FROM TransactionHistory t
        WHERE t.transactionStatus = :status
          AND t.transactionTime >= :from
          AND t.transactionTime < :to
        GROUP BY t.fromUserId
        ORDER BY SUM(t.amount) DESC
    """)
    List<TopUserProjection> findTopUsers(
        ETransactionStatus status,
        LocalDateTime from,
        LocalDateTime to
    );
}
