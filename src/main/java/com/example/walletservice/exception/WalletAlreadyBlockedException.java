package com.example.walletservice.exception;

public class WalletAlreadyBlockedException extends RuntimeException {
    public WalletAlreadyBlockedException(String message) {
        super(message);
    }
}
