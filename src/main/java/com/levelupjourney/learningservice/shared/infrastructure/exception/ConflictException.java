package com.levelupjourney.learningservice.shared.infrastructure.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
