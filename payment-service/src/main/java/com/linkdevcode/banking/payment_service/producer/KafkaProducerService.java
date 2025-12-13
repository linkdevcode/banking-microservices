package com.linkdevcode.banking.payment_service.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.linkdevcode.banking.payment_service.event.TransactionCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    
    private static String HISTORY_TOPIC = "transaction-completed-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Sends a transaction completion event to Kafka for the History Service to comsume.
     */
    public void sendTransactionCompletedEvent(TransactionCompletedEvent event) {
        log.info("Sending Transaction Completed Event to Kafka: {}", event);
        kafkaTemplate.send(HISTORY_TOPIC, event.getTransactionId().toString(), event);
    }
}
