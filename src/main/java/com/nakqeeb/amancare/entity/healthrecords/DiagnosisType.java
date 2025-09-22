package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Diagnosis Type Enum - نوع التشخيص
// =============================================================================
public enum DiagnosisType {
    PRIMARY("أساسي"),
    SECONDARY("ثانوي"),
    DIFFERENTIAL("تشخيص تفريقي"),
    PROVISIONAL("مؤقت"),
    FINAL("نهائي"),
    RULED_OUT("مستبعد");

    private final String arabicName;

    DiagnosisType(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
