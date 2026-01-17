package com.linkdevcode.banking.history_service.model.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopUserStatistic {

    private Long userId;
    private BigDecimal totalAmount;
}