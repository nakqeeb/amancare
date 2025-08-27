package com.nakqeeb.amancare.exception;

/**
 * استثناء المورد غير الموجود
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}