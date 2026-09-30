package com.example.walletservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** A processed Idempotency-Key; unique per user, so a retried transfer is not executed twice. */
@Entity
@Table(name = "idempotency_keys")
@Getter
@NoArgsConstructor
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String key;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public IdempotencyKey(Long userId, String key) {
        this.userId = userId;
        this.key = key;
    }
}
