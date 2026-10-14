package com.unipoli.inventory.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ValidationError> validationErrors
) {
    public record ValidationError(String field, String message) {}
}
