package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء انتهاء مهلة السجل الطبي
 * Exception thrown when medical record operations exceed time limits
 */
public class MedicalRecordTimeoutException extends BusinessLogicException {

    public MedicalRecordTimeoutException(String message) {
        super(message);
    }

    public static MedicalRecordTimeoutException editingTimeExpired(Long medicalRecordId) {
        return new MedicalRecordTimeoutException(
                "انتهت مهلة تعديل السجل الطبي: " + medicalRecordId);
    }

    public static MedicalRecordTimeoutException followUpOverdue(Long medicalRecordId) {
        return new MedicalRecordTimeoutException(
                "موعد المتابعة متأخر للسجل الطبي: " + medicalRecordId);
    }
}
