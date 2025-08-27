package com.nakqeeb.amancare.exception;

/**
 * استثناء الطلب غير الصحيح
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}