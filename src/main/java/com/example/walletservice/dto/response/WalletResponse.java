package com.example.walletservice.dto.response;

import com.example.walletservice.enums.CurrencyEnum;
import com.example.walletservice.enums.WalletStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class WalletResponse {

    private Long id;
    private Long userId;
    private BigDecimal balance;
    private CurrencyEnum currency;
    private WalletStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
