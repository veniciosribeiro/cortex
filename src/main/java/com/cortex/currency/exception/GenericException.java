package com.cortex.currency.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GenericException extends RuntimeException {
    private HttpStatus httpStatus;

    public GenericException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public GenericException(String message, Throwable throwable, HttpStatus httpStatus) {
        super(message, throwable);
        this.httpStatus = httpStatus;
    }
}
