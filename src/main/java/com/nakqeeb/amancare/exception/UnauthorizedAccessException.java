// UnauthorizedAccessException.java  
package com.nakqeeb.amancare.exception;

/**
 * Base exception for unauthorized access attempts
 * استثناء أساسي لمحاولات الوصول غير المصرح بها
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}