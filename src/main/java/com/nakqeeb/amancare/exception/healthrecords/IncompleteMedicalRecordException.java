package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء عدم اكتمال السجل الطبي
 * Exception thrown when trying to complete an incomplete medical record
 */
public class IncompleteMedicalRecordException extends BusinessLogicException {

    public IncompleteMedicalRecordException(String message) {
        super(message);
    }

    public static IncompleteMedicalRecordException missingChiefComplaint() {
        return new IncompleteMedicalRecordException("الشكوى الرئيسية مطلوبة");
    }

    public static IncompleteMedicalRecordException missingPhysicalExamination() {
        return new IncompleteMedicalRecordException("الفحص السريري مطلوب");
    }

    public static IncompleteMedicalRecordException missingTreatmentPlan() {
        return new IncompleteMedicalRecordException("خطة العلاج مطلوبة");
    }

    public static IncompleteMedicalRecordException missingDiagnosis() {
        return new IncompleteMedicalRecordException("التشخيص مطلوب");
    }
}
