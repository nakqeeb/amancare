// =============================================================================
// Medical Records Exception Classes - استثناءات السجلات الطبية
// src/main/java/com/nakqeeb/amancare/exception/
// =============================================================================

package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء عام للسجلات الطبية
 * Base exception for all medical record related errors
 */
public class MedicalRecordException extends BusinessLogicException {

    public MedicalRecordException(String message) {
        super(message);
    }

    public MedicalRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}

// =============================================================================
// Exception Handler Responses
// =============================================================================

