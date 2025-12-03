package com.linkdevcode.banking.payment_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.linkdevcode.banking.payment_service.entity.Transaction;

/**
 * Repository for Transaction entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds paginated transactions where the user is either the sender or the recipient.
     */
    Page<Transaction> findBySenderIdOrRecipientId(Long senderId, Long recipientId, Pageable pageable);
}