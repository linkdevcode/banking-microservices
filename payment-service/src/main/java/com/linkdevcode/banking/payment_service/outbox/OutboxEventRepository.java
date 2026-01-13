package com.linkdevcode.banking.payment_service.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.linkdevcode.banking.payment_service.entity.OutboxEvent;
import com.linkdevcode.banking.payment_service.enumeration.EOutboxStatus;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    
    // Fetch top 50 outbox events with NEW status ordered by creation time
    List<OutboxEvent> findTop50ByStatusOrderByCreatedAt(EOutboxStatus status);
}
