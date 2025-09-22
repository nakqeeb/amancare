package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Record Status Enum - حالة السجل
// =============================================================================
public enum RecordStatus {
    DRAFT("مسودة"),
    IN_PROGRESS("قيد التحرير"),
    COMPLETED("مكتمل"),
    REVIEWED("مراجع"),
    LOCKED("مقفل"),
    CANCELLED("ملغي");

    private final String arabicName;

    RecordStatus(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
