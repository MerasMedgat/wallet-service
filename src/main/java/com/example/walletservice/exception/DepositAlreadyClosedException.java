package com.example.walletservice.exception;

public class DepositAlreadyClosedException extends RuntimeException {
    public DepositAlreadyClosedException(String message) {
        super(message);
    }
}
