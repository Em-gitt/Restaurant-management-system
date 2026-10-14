package com.unipoli.delivery.exception;

public class DuplicateWebhookException extends RuntimeException {
    public DuplicateWebhookException(String message) {
        super(message);
    }
}
