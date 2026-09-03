package com.example.walletservice.exception;

public class TransferToSameWalletException extends RuntimeException {
    public TransferToSameWalletException(String message) {
        super(message);
    }
}
