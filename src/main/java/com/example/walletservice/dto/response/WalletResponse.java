package com.example.walletservice.dto.response;

import com.example.walletservice.enums.CurrencyEnum;
import com.example.walletservice.enums.WalletStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
        Long id,
        Long userId,
        BigDecimal balance,
        CurrencyEnum currency,
        WalletStatusEnum status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
