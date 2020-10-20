package com.cortex.currency.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RabbitException extends GenericException {
    private HttpStatus httpStatus;

    public RabbitException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RabbitException(String message, Throwable throwable, HttpStatus httpStatus) {
        super(message, throwable);
        this.httpStatus = httpStatus;
    }
}
