package com.example.walletservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base class for business errors: carries the HTTP status it should be reported with.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
