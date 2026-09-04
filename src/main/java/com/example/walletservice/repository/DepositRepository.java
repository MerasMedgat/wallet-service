package com.example.walletservice.repository;

import com.example.walletservice.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositRepository extends JpaRepository<Deposit,Long>{
}
