package com.example.walletservice.dto.response;

import com.example.walletservice.enums.DepositStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DepositResponse {
    private Long id;
    private Long walletId;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private LocalDate startDate;
    private LocalDate endDate;
    private DepositStatusEnum depositStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
