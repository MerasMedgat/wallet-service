package com.example.walletservice.exception;

public class WalletAlreadyActiveException extends RuntimeException {
    public WalletAlreadyActiveException(String message) {
        super(message);
    }
}
