package com.example.walletservice.dto.request;

import com.example.walletservice.entity.CurrencyEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WalletRequest {

    @NotNull
    private CurrencyEnum currency;
}
