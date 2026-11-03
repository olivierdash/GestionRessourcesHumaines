package com.example.sirh.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Transforme les exceptions métier en JSON { "message": "..." }.
 * Limité au package controller.api pour ne pas interférer avec les controllers Thymeleaf.
 */
@RestControllerAdvice(basePackages = "com.example.sirh.controller.api.v1")
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        return build(ex.getStatusCode().value(), ex.getReason());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return build(404, ex.getMessage());
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        return build(400, ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(RuntimeException ex) {
        return build(400, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(int status, String message) {
        String body = message == null || message.isBlank() ? "Erreur inconnue" : message;
        return ResponseEntity.status(status).body(Map.of("message", body));
    }
}