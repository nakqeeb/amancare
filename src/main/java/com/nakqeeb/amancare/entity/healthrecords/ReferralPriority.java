package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Referral Priority Enum - أولوية التحويل
// =============================================================================
public enum ReferralPriority {
    ROUTINE("عادي"),
    URGENT("عاجل"),
    EMERGENCY("طارئ");

    private final String arabicName;

    ReferralPriority(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
