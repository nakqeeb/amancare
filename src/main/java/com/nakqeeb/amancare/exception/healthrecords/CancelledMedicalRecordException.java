package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء السجل الطبي الملغي
 * Exception thrown when trying to access or modify a cancelled medical record
 */
public class CancelledMedicalRecordException extends BusinessLogicException {

    public CancelledMedicalRecordException() {
        super("السجل الطبي ملغي ولا يمكن الوصول إليه");
    }

    public CancelledMedicalRecordException(Long medicalRecordId) {
        super("السجل الطبي ملغي: " + medicalRecordId);
    }

    public CancelledMedicalRecordException(String message) {
        super(message);
    }
}
