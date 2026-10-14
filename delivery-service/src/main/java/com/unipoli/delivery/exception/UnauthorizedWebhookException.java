package com.unipoli.delivery.exception;

public class UnauthorizedWebhookException extends RuntimeException {
    public UnauthorizedWebhookException(String message) {
        super(message);
    }
}
