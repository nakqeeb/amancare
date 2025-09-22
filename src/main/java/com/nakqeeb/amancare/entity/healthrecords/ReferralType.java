package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Referral Type Enum - نوع التحويل
// =============================================================================
public enum ReferralType {
    SPECIALIST("أخصائي"),
    HOSPITAL("مستشفى"),
    EMERGENCY("طوارئ"),
    LABORATORY("مختبر"),
    RADIOLOGY("أشعة"),
    PHYSIOTHERAPY("علاج طبيعي"),
    PSYCHIATRY("طب نفسي"),
    DENTISTRY("طب أسنان"),
    OPHTHALMOLOGY("طب عيون"),
    ENT("أنف وأذن وحنجرة");

    private final String arabicName;

    ReferralType(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
