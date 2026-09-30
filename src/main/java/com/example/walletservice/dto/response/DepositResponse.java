package com.example.walletservice.dto.response;

import com.example.walletservice.enums.DepositStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DepositResponse(
        Long id,
        Long walletId,
        BigDecimal amount,
        BigDecimal interestRate,
        Integer termMonths,
        LocalDate startDate,
        LocalDate endDate,
        DepositStatusEnum depositStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
