package com.example.walletservice.exception;

public class InvalidDepositTermException extends RuntimeException {
    public InvalidDepositTermException(String message) {
        super(message);
    }
}
