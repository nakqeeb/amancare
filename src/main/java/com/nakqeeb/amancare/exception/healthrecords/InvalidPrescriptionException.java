package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء الوصفة الطبية غير صالحة
 * Exception thrown when prescription validation fails
 */
public class InvalidPrescriptionException extends BusinessLogicException {

    public InvalidPrescriptionException(String message) {
        super(message);
    }

    public static InvalidPrescriptionException invalidDosage(String medication) {
        return new InvalidPrescriptionException("جرعة غير صالحة للدواء: " + medication);
    }

    public static InvalidPrescriptionException invalidDuration(String medication) {
        return new InvalidPrescriptionException("مدة علاج غير صالحة للدواء: " + medication);
    }

    public static InvalidPrescriptionException invalidRoute(String medication) {
        return new InvalidPrescriptionException("طريقة إعطاء غير صالحة للدواء: " + medication);
    }
}
