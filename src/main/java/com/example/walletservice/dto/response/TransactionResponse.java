package com.example.walletservice.dto.response;

import com.example.walletservice.enums.TransactionTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long walletId,
        TransactionTypeEnum transactionType,
        BigDecimal amount,
        BigDecimal balanceAfterTransaction,
        LocalDateTime createdAt
) {
}
