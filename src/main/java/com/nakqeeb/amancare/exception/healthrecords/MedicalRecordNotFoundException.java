package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.ResourceNotFoundException;

/**
 * استثناء السجل الطبي غير موجود
 * Exception thrown when a medical record is not found
 */
public class MedicalRecordNotFoundException extends ResourceNotFoundException {

    public MedicalRecordNotFoundException(Long medicalRecordId) {
        super("السجل الطبي غير موجود: " + medicalRecordId);
    }

    public MedicalRecordNotFoundException(String message) {
        super(message);
    }
}
