package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Visit Type Enum - نوع الزيارة
// =============================================================================
public enum VisitType {
    CONSULTATION("استشارة"),
    FOLLOW_UP("متابعة"),
    EMERGENCY("طارئة"),
    ROUTINE_CHECKUP("فحص دوري"),
    VACCINATION("تطعيم"),
    PROCEDURE("إجراء طبي"),
    SURGERY("عملية جراحية"),
    REHABILITATION("تأهيل"),
    PREVENTIVE_CARE("رعاية وقائية"),
    CHRONIC_CARE("رعاية مزمنة");

    private final String arabicName;

    VisitType(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}

