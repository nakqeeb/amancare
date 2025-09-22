package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Procedure Category Enum - فئة الإجراء
// =============================================================================
public enum ProcedureCategory {
    DIAGNOSTIC("تشخيصي"),
    THERAPEUTIC("علاجي"),
    SURGICAL("جراحي"),
    PREVENTIVE("وقائي"),
    COSMETIC("تجميلي"),
    EMERGENCY("طارئ"),
    REHABILITATION("تأهيلي");

    private final String arabicName;

    ProcedureCategory(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
