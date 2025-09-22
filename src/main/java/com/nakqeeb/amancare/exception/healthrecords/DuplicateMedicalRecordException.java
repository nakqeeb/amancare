package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء تضارب السجل الطبي مع الموعد
 * Exception thrown when trying to create multiple medical records for the same appointment
 */
public class DuplicateMedicalRecordException extends BusinessLogicException {

    public DuplicateMedicalRecordException() {
        super("يوجد سجل طبي مرتبط بهذا الموعد بالفعل");
    }

    public DuplicateMedicalRecordException(Long appointmentId) {
        super("يوجد سجل طبي مرتبط بالموعد " + appointmentId + " بالفعل");
    }

    public DuplicateMedicalRecordException(String message) {
        super(message);
    }
}
