package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Test Urgency Enum - أولوية الفحص
// =============================================================================
public enum TestUrgency {
    ROUTINE("عادي"),
    URGENT("عاجل"),
    STAT("فوري"),
    ASAP("بأسرع وقت");

    private final String arabicName;

    TestUrgency(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
