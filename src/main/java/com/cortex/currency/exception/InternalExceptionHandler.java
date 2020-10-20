package com.cortex.currency.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class InternalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(BancoCentralException.class);

    @ExceptionHandler(BancoCentralException.class)
    public void handleSQLException(Exception ex) {
        logger.info("SQLException Occured:: URL=");
    }
}
