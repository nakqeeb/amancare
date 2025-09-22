package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء حد أقصى للسجلات الطبية
 * Exception thrown when exceeding limits on medical records
 */
public class MedicalRecordLimitException extends BusinessLogicException {

    public MedicalRecordLimitException(String message) {
        super(message);
    }

    public static MedicalRecordLimitException tooManyRecordsPerDay(Long patientId) {
        return new MedicalRecordLimitException(
                "تم الوصول للحد الأقصى من السجلات الطبية لهذا اليوم للمريض: " + patientId);
    }

    public static MedicalRecordLimitException tooManyAttachments() {
        return new MedicalRecordLimitException("تم الوصول للحد الأقصى من المرفقات");
    }
}
