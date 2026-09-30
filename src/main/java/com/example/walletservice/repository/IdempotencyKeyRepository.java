package com.example.walletservice.repository;

import com.example.walletservice.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    boolean existsByUserIdAndKey(Long userId, String key);
}
