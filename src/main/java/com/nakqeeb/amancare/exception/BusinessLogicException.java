// BusinessLogicException.java
package com.nakqeeb.amancare.exception;

/**
 * Base exception for business logic errors
 * استثناء أساسي لأخطاء منطق الأعمال
 */
public class BusinessLogicException extends RuntimeException {

    public BusinessLogicException(String message) {
        super(message);
    }

    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}