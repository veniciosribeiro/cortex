package com.cortex.currency.exception;

public class BancoCentralException extends RuntimeException {
    public BancoCentralException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public BancoCentralException(String message) {
        super(message);
    }
}
