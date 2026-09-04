package com.example.walletservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositRequest {
    @NotNull
    private Long walletId;
    @NotNull
    @DecimalMin(value="0.01")
    private BigDecimal amount;
    @NotNull
    private Integer termMonths;

}
