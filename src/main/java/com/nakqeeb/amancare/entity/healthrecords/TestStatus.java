package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Test Status Enum - حالة الفحص
// =============================================================================
public enum TestStatus {
    ORDERED("مطلوب"),
    COLLECTED("تم السحب"),
    IN_PROGRESS("قيد التحليل"),
    COMPLETED("مكتمل"),
    CANCELLED("ملغي"),
    DELAYED("مؤجل");

    private final String arabicName;

    TestStatus(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
