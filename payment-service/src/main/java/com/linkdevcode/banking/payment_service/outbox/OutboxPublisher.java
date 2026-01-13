package com.linkdevcode.banking.payment_service.outbox;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkdevcode.banking.payment_service.entity.OutboxEvent;
import com.linkdevcode.banking.payment_service.enumeration.EOutboxStatus;
import com.linkdevcode.banking.payment_service.event.TransactionCompletedEvent;
import com.linkdevcode.banking.payment_service.producer.KafkaProducerService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private static final String HISTORY_TOPIC = "transaction-completed-topic";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents(){
        List<OutboxEvent> outboxEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAt(EOutboxStatus.NEW);

        if (outboxEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent outboxEvent : outboxEvents) {
            try {
                log.info("Publishing outbox event id={}", outboxEvent.getId());

                TransactionCompletedEvent eventObject =
                    objectMapper.readValue(
                        outboxEvent.getPayload(),
                        TransactionCompletedEvent.class
                    );

                // Send event to Kafka topic
                kafkaProducerService.send(
                    HISTORY_TOPIC,
                    outboxEvent.getAggregateId(), 
                    eventObject);

                // Update outbox event status to SENT
                outboxEvent.setStatus(EOutboxStatus.SENT);
                outboxEventRepository.save(outboxEvent);

                log.info("Outbox event {} published successfully", outboxEvent.getId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", outboxEvent.getId(), e.getMessage());
            }
        }
    }    
}
