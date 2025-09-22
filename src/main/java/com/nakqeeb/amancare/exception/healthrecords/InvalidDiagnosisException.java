package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء التشخيص غير صالح
 * Exception thrown when diagnosis validation fails
 */
public class InvalidDiagnosisException extends BusinessLogicException {

    public InvalidDiagnosisException(String message) {
        super(message);
    }

    public static InvalidDiagnosisException noPrimaryDiagnosis() {
        return new InvalidDiagnosisException("يجب تحديد تشخيص أساسي واحد على الأقل");
    }

    public static InvalidDiagnosisException multiplePrimaryDiagnoses() {
        return new InvalidDiagnosisException("لا يمكن تحديد أكثر من تشخيص أساسي واحد");
    }

    public static InvalidDiagnosisException emptyDiagnosis() {
        return new InvalidDiagnosisException("يجب إدراج تشخيص واحد على الأقل");
    }
}
