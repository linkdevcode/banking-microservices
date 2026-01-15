package com.linkdevcode.banking.batch_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkdevcode.banking.batch_service.entity.BatchTempItem;

public interface BatchTempItemRepository extends JpaRepository<BatchTempItem, Long> {
    
    List<BatchTempItem> findByBatchTempIdAndValidTrue(String tempId);
}
