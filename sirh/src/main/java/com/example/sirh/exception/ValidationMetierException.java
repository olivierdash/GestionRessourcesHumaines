package com.example.sirh.exception;

import java.util.Map;

public class ValidationMetierException extends RuntimeException {
    private final Map<String, String> errors;

    public ValidationMetierException(Map<String, String> errors) {
        super("Erreurs de validation métier");
        this.errors = errors;
    }

    public ValidationMetierException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}