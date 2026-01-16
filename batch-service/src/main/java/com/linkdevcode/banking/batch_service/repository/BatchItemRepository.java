package com.linkdevcode.banking.batch_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkdevcode.banking.batch_service.entity.BatchItem;
import com.linkdevcode.banking.batch_service.enumeration.EBatchItemStatus;

public interface BatchItemRepository extends JpaRepository<BatchItem, Long> {
    
    List<BatchItem> findByBatchJobIdAndStatus(Long id, EBatchItemStatus status);
}
