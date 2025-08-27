package com.nakqeeb.amancare.exception;

/**
 * استثناء الوصول غير المصرح به
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
