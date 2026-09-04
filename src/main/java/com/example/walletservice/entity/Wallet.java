package com.example.walletservice.entity;

import com.example.walletservice.enums.CurrencyEnum;
import com.example.walletservice.enums.WalletStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="user_id",nullable=false)
    private User user;
    @Column(nullable=false,scale=2,precision=19)
    private BigDecimal balance;
    @Column(nullable=false,length=3)
    @Enumerated(EnumType.STRING)
    private CurrencyEnum currency;
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private WalletStatusEnum status;
    @Column(nullable=false,updatable=false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(nullable=false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
