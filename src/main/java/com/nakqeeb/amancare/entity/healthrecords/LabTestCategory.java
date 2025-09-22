package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Lab Test Category Enum - فئة الفحص المخبري
// =============================================================================
public enum LabTestCategory {
    HEMATOLOGY("أمراض الدم"),
    BIOCHEMISTRY("الكيمياء الحيوية"),
    MICROBIOLOGY("الأحياء الدقيقة"),
    IMMUNOLOGY("المناعة"),
    ENDOCRINOLOGY("الغدد الصماء"),
    CARDIOLOGY("القلب"),
    NEPHROLOGY("الكلى"),
    HEPATOLOGY("الكبد"),
    ONCOLOGY("الأورام"),
    TOXICOLOGY("السموم"),
    GENETICS("الوراثة"),
    COAGULATION("التخثر");

    private final String arabicName;

    LabTestCategory(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
