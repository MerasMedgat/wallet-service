package com.example.walletservice.entity;

import com.example.walletservice.enums.DepositStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="wallet_id",nullable=false)
    private Wallet wallet;
    @Column(nullable=false)
    private BigDecimal amount;
    @Column(nullable=false)
    private BigDecimal interestRate;
    @Column(nullable=false)
    private int termMonths;
    @Column(nullable=false)
    private LocalDate startDate;
    @Column(nullable=false)
    private LocalDate endDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private DepositStatusEnum Status;
    @CreationTimestamp
    @Column(nullable=false,updatable=false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable=false)
    private LocalDateTime updatedAt;

}
