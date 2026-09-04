package com.example.walletservice.dto.response;

import com.example.walletservice.enums.TransactionTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponse {

    private Long id;
    private Long walletId;
    private TransactionTypeEnum transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private LocalDateTime createdAt;
}
