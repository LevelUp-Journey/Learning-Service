package com.levelupjourney.learningservice.shared.infrastructure.exception;

/**
 * Exception thrown when search parameters are invalid or missing
 */
public class InvalidSearchCriteriaException extends RuntimeException {
    
    public InvalidSearchCriteriaException(String message) {
        super(message);
    }
    
    public InvalidSearchCriteriaException(String message, Throwable cause) {
        super(message, cause);
    }
}
