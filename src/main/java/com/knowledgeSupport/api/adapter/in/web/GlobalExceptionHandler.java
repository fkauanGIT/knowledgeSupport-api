package com.knowledgeSupport.api.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.NoSuchElementException;

/**
 * "Chamado/Standard não existe" é erro de cliente (404), não falha de servidor (500).
 * Centraliza aqui em vez de cada controller capturar NoSuchElementException na mão.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), "Not Found", e.getMessage()));
    }

    public record ErrorResponse(Instant timestamp, int status, String error, String message) {}
}
