package com.nakqeeb.amancare.exception.healthrecords;

/**
 * استجابة استثناءات السجلات الطبية
 * Custom exception response for medical record errors
 */
public class MedicalRecordExceptionResponse {
    private String errorCode;
    private String errorMessage;
    private String arabicMessage;
    private Long medicalRecordId;
    private String suggestion;

    // Constructors, getters, and setters...

    public MedicalRecordExceptionResponse() {
    }

    public MedicalRecordExceptionResponse(String errorCode, String errorMessage, String arabicMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.arabicMessage = arabicMessage;
    }

    public MedicalRecordExceptionResponse(String errorCode, String errorMessage, String arabicMessage,
                                          Long medicalRecordId, String suggestion) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.arabicMessage = arabicMessage;
        this.medicalRecordId = medicalRecordId;
        this.suggestion = suggestion;
    }

    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getArabicMessage() {
        return arabicMessage;
    }

    public void setArabicMessage(String arabicMessage) {
        this.arabicMessage = arabicMessage;
    }

    public Long getMedicalRecordId() {
        return medicalRecordId;
    }

    public void setMedicalRecordId(Long medicalRecordId) {
        this.medicalRecordId = medicalRecordId;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}
