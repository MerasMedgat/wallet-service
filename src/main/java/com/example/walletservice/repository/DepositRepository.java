package com.example.walletservice.repository;

import com.example.walletservice.entity.Deposit;
import com.example.walletservice.exception.NotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DepositRepository extends JpaRepository<Deposit, Long> {

    Page<Deposit> findByWalletId(Long walletId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Deposit d where d.id = :id")
    Optional<Deposit> findByIdForUpdate(@Param("id") Long id);

    default Deposit getOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("Deposit " + id + " not found"));
    }

    default Deposit lockOrThrow(Long id) {
        return findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Deposit " + id + " not found"));
    }
}
