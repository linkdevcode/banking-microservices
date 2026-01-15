package com.linkdevcode.banking.batch_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkdevcode.banking.batch_service.entity.BatchTemp;

public interface BatchTempRepository extends JpaRepository<BatchTemp, String> {
    
}
