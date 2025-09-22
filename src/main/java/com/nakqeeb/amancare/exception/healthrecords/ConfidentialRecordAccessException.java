package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.UnauthorizedAccessException;

/**
 * استثناء الوصول غير المصرح للسجل السري
 * Exception thrown when trying to access confidential medical records without proper authorization
 */
public class ConfidentialRecordAccessException extends UnauthorizedAccessException {

    public ConfidentialRecordAccessException() {
        super("غير مصرح بالوصول لهذا السجل السري");
    }

    public ConfidentialRecordAccessException(String message) {
        super(message);
    }

    public ConfidentialRecordAccessException(Long medicalRecordId) {
        super("غير مصرح بالوصول للسجل الطبي السري: " + medicalRecordId);
    }
}
