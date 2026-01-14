package com.linkdevcode.banking.history_service.consumer;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.linkdevcode.banking.history_service.entity.TransactionHistory;
import com.linkdevcode.banking.history_service.event.TransactionCompletedEvent;
import com.linkdevcode.banking.history_service.repository.TransactionHistoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionHistoryConsumer {
    
    private final TransactionHistoryRepository transactionHistoryRepository;
    
    @KafkaListener(
        topics = "transaction-completed-topic",
        groupId = "history-service"
    )
    @Transactional
    public void consumeTransactionCompletedEvent(
        TransactionCompletedEvent event,
        Acknowledgment ack) {
        try {
            log.info("Received TransactionCompletedEvent: {}", event);

            // Check if the transaction history already exists
            if (transactionHistoryRepository.existsByTransactionId(event.getTransactionId())) {
                log.warn("Transaction history already exists for transactionId: {}", event.getTransactionId());
                ack.acknowledge();
                return;
            }

            // Map event to entity
            TransactionHistory transactionHistory = new TransactionHistory();
            transactionHistory.setTransactionId(event.getTransactionId());
            transactionHistory.setFromUserId(event.getFromUserId());
            transactionHistory.setToUserId(event.getToUserId());
            transactionHistory.setFromAccountNumber(event.getFromAccountNumber());
            transactionHistory.setToAccountNumber(event.getToAccountNumber());
            transactionHistory.setAmount(event.getAmount());
            transactionHistory.setTransactionType(event.getTransactionType());
            transactionHistory.setStatus(event.getStatus());
            transactionHistory.setMessage(event.getMessage());
            transactionHistory.setTransactionTime(event.getTransactionTime());
            transactionHistory.setRecordedAt(LocalDateTime.now());

            // Save to repository
            transactionHistoryRepository.save(transactionHistory);
            log.info("Saved TransactionHistory for transactionId: {}", event.getTransactionId());

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing TransactionCompletedEvent: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
