package com.knowledgeSupport.api.adapter.in.web;

import com.knowledgeSupport.api.adapter.out.jira.JiraCredentialsInvalidException;
import com.knowledgeSupport.api.application.service.UnsupportedDocumentTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Tratamento central de erros. Cada classe de falha vira o status correto, e o 500 é
 * sanitizado (loga o stacktrace, devolve mensagem genérica) para não vazar internals.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** "Ticket/Standard/Documento não existe" é erro do cliente (404). */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException e) {
        return build(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
    }

    @ExceptionHandler(JiraCredentialsInvalidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJiraCredentials(JiraCredentialsInvalidException e) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage());
    }

    @ExceptionHandler(UnsupportedDocumentTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedDocumentType(UnsupportedDocumentTypeException e) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage());
    }

    /** Corpo inválido (Bean Validation): 400 com o detalhe dos campos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "Bad Request", detail.isBlank() ? "Invalid request" : detail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage());
    }

    /** Jira indisponível/lento (timeout, conexão recusada): dependência externa fora, não é 500 nosso. */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamUnavailable(ResourceAccessException e) {
        log.warn("Falha de conectividade com dependência externa (Jira): {}", e.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                "Serviço externo (Jira) indisponível no momento. Tente novamente em instantes.");
    }

    /** Jira respondeu com erro (4xx/5xx): traduz para 502 sem vazar o corpo do upstream. */
    @ExceptionHandler({HttpServerErrorException.class, HttpClientErrorException.class})
    public ResponseEntity<ErrorResponse> handleUpstreamError(Exception e) {
        log.warn("Erro retornado pela dependência externa (Jira): {}", e.getMessage());
        return build(HttpStatus.BAD_GATEWAY, "Bad Gateway", "Erro ao consultar o Jira.");
    }

    /** Rede de segurança: loga o stacktrace, devolve mensagem genérica. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Erro inesperado ao processar a requisição", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Erro interno inesperado.");
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(), error, message));
    }

    public record ErrorResponse(Instant timestamp, int status, String error, String message) {}
}
