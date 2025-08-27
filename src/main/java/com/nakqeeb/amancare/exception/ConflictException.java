package com.nakqeeb.amancare.exception;

/**
 * استثناء التضارب في البيانات
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}