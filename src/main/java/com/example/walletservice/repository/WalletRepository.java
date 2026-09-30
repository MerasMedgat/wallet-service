package com.example.walletservice.repository;

import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.CurrencyEnum;
import com.example.walletservice.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByUserId(Long userId);

    boolean existsByUserIdAndCurrency(Long userId, CurrencyEnum currency);

    default Wallet getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Wallet " + id + " not found"));
    }
}
