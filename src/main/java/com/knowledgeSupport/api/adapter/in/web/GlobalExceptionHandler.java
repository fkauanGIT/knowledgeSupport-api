package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.adapter.out.jira.JiraCredentialsInvalidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.NoSuchElementException;

/**
 * "Ticket/Standard doesn't exist" is a client error (404), not a server failure (500).
 * Centralized here instead of each controller catching NoSuchElementException by hand.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), "Not Found", e.getMessage()));
    }

    @ExceptionHandler(JiraCredentialsInvalidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJiraCredentials(JiraCredentialsInvalidException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage()));
    }

    public record ErrorResponse(Instant timestamp, int status, String error, String message) {}
}
