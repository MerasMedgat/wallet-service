package com.example.walletservice.repository;

import com.example.walletservice.entity.Deposit;
import com.example.walletservice.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositRepository extends JpaRepository<Deposit, Long> {

    List<Deposit> findByWalletId(Long walletId);

    default Deposit getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Deposit " + id + " not found"));
    }
}
