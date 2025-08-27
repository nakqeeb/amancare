package com.nakqeeb.amancare.exception;

/**
 * استثناء انتهاء صلاحية الرمز المميز
 */
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}