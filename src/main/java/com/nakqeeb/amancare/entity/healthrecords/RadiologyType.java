package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Radiology Type Enum - نوع الأشعة
// =============================================================================
public enum RadiologyType {
    X_RAY("أشعة سينية"),
    CT_SCAN("أشعة مقطعية"),
    MRI("رنين مغناطيسي"),
    ULTRASOUND("موجات فوق صوتية"),
    MAMMOGRAPHY("تصوير الثدي"),
    BONE_SCAN("مسح العظام"),
    PET_SCAN("مسح البوزيترون"),
    ANGIOGRAPHY("تصوير الأوعية"),
    FLUOROSCOPY("تنظير الأشعة"),
    NUCLEAR_MEDICINE("طب نووي");

    private final String arabicName;

    RadiologyType(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
