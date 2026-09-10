package com.example.walletservice.exception;

public class DepositAlreadyCompletedException extends RuntimeException {
    public DepositAlreadyCompletedException(String message) {
        super(message);
    }
}
