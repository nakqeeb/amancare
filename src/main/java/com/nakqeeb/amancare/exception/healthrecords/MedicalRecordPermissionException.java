package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.UnauthorizedAccessException;

/**
 * استثناء صلاحيات السجل الطبي
 * Exception thrown when user doesn't have proper permissions for medical record operations
 */
public class MedicalRecordPermissionException extends UnauthorizedAccessException {

    public MedicalRecordPermissionException(String message) {
        super(message);
    }

    public static MedicalRecordPermissionException cannotCreateRecord() {
        return new MedicalRecordPermissionException("غير مصرح بإنشاء السجلات الطبية");
    }

    public static MedicalRecordPermissionException cannotModifyRecord(Long medicalRecordId) {
        return new MedicalRecordPermissionException("غير مصرح بتعديل السجل الطبي: " + medicalRecordId);
    }

    public static MedicalRecordPermissionException cannotDeleteRecord(Long medicalRecordId) {
        return new MedicalRecordPermissionException("غير مصرح بحذف السجل الطبي: " + medicalRecordId);
    }

    public static MedicalRecordPermissionException cannotAccessRecord(Long medicalRecordId) {
        return new MedicalRecordPermissionException("غير مصرح بالوصول للسجل الطبي: " + medicalRecordId);
    }

    public static MedicalRecordPermissionException onlyDoctorsCanCreateRecords() {
        return new MedicalRecordPermissionException("فقط الأطباء يمكنهم إنشاء السجلات الطبية");
    }
}
