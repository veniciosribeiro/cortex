package com.cortex.currency.exception;

import com.cortex.currency.dto.RestResponseErrorDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {GenericException.class})
    protected ResponseEntity<Object> handleConflict(GenericException e, WebRequest request) {
        RestResponseErrorDTO restResponseErrorDTO = RestResponseErrorDTO.builder()
                .status(e.getHttpStatus().value())
                .message(e.getMessage())
                .build();
        e.printStackTrace();
        return handleExceptionInternal(e, restResponseErrorDTO, new HttpHeaders(), e.getHttpStatus(), request);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleConflict(Exception e, WebRequest request) {
        RestResponseErrorDTO restResponseErrorDTO = RestResponseErrorDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Erro desconhecido")
                .build();
        e.printStackTrace();
        return handleExceptionInternal(e, restResponseErrorDTO, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
