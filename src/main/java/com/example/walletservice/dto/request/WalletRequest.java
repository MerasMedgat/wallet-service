package com.example.walletservice.dto.request;

import com.example.walletservice.enums.CurrencyEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletRequest {

    @NotNull
    private CurrencyEnum currency;
}
