package com.example.walletservice.exception;

public class FromWalletIdNotFoundException extends RuntimeException {
    public FromWalletIdNotFoundException(String message) {
        super(message);
    }
}
