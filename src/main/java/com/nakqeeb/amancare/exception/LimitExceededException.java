package com.nakqeeb.amancare.exception;

/**
 * استثناء تجاوز الحد المسموح
 */
public class LimitExceededException extends RuntimeException {
    public LimitExceededException(String message) {
        super(message);
    }
}