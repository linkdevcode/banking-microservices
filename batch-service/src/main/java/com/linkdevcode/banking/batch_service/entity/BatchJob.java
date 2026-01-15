package com.linkdevcode.banking.batch_service.entity;

import java.time.LocalDateTime;

import com.linkdevcode.banking.batch_service.enumeration.EBatchJobStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "batch_job")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_temp_id")
    private String batchTempId;

    @Column(name = "total_items")
    private int totalItems;

    @Column(name = "success_items")
    private int successItems;

    @Column(name = "failed_items")
    private int failedItems;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EBatchJobStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
}
