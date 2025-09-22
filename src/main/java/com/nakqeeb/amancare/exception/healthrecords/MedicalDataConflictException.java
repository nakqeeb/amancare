package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء تعارض في البيانات الطبية
 * Exception thrown when there's a conflict in medical data
 */
public class MedicalDataConflictException extends BusinessLogicException {

    public MedicalDataConflictException(String message) {
        super(message);
    }

    public static MedicalDataConflictException appointmentMismatch(Long appointmentId, Long patientId) {
        return new MedicalDataConflictException(
                "الموعد " + appointmentId + " غير مرتبط بالمريض " + patientId);
    }

    public static MedicalDataConflictException doctorMismatch(Long doctorId, Long appointmentId) {
        return new MedicalDataConflictException(
                "الطبيب " + doctorId + " غير مرتبط بالموعد " + appointmentId);
    }

    public static MedicalDataConflictException visitDateMismatch(String recordDate, String appointmentDate) {
        return new MedicalDataConflictException(
                "تاريخ السجل الطبي " + recordDate + " لا يتطابق مع تاريخ الموعد " + appointmentDate);
    }
}
