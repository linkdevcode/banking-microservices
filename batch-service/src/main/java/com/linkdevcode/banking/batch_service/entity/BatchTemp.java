package com.linkdevcode.banking.batch_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "batch_temp")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchTemp {
    
    @Id
    private String id;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "total_records")
    private int totalRecords;

    @Column(name = "valid_records")
    private int validRecords;

    @Column(name = "invalid_records")
    private int invalidRecords;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
