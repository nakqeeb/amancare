package com.nakqeeb.amancare.exception;

/**
 * استثناء العملية غير المسموحة
 */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}