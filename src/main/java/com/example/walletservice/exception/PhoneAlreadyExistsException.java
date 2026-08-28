package com.example.walletservice.exception;

public class PhoneAlreadyExistsException extends RuntimeException{
    public PhoneAlreadyExistsException(String message){
        super(message);
    }
}
