package com.linkdevcode.banking.history_service.repository.projection;

import java.math.BigDecimal;

public interface TopUserProjection {
    Long getUserId();
    BigDecimal getTotalAmount();
}
