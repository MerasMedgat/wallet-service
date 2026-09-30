package com.example.walletservice.repository;

import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.CurrencyEnum;
import com.example.walletservice.exception.NotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByUserId(Long userId);

    boolean existsByUserIdAndCurrency(Long userId, CurrencyEnum currency);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);

    default Wallet getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Wallet " + id + " not found"));
    }

    default Wallet lockOrThrow(Long id) {
        return findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Wallet " + id + " not found"));
    }
}
