package com.example.walletservice.repository;

import com.example.walletservice.enums.CurrencyEnum;
import com.example.walletservice.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet,Long> {

    List<Wallet> findByUserId(Long userId);
    boolean existsByUserIdAndCurrency(Long userId , CurrencyEnum currency);
}
