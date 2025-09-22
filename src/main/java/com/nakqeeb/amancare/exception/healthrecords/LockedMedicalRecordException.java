package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء السجل الطبي مقفل
 * Exception thrown when trying to modify a locked medical record
 */
public class LockedMedicalRecordException extends BusinessLogicException {

    public LockedMedicalRecordException() {
        super("لا يمكن تعديل السجل الطبي المقفل");
    }

    public LockedMedicalRecordException(Long medicalRecordId) {
        super("السجل الطبي مقفل ولا يمكن تعديله: " + medicalRecordId);
    }

    public LockedMedicalRecordException(String message) {
        super(message);
    }
}
