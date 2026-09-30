package com.example.walletservice.dto.request;

import com.example.walletservice.enums.CurrencyEnum;
import jakarta.validation.constraints.NotNull;

public record WalletRequest(@NotNull CurrencyEnum currency) {
}
