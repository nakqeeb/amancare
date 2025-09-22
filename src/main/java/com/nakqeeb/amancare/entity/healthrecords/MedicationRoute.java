package com.nakqeeb.amancare.entity.healthrecords;

// =============================================================================
// Medication Route Enum - طريقة إعطاء الدواء
// =============================================================================
public enum MedicationRoute {
    ORAL("عن طريق الفم"),
    TOPICAL("موضعي"),
    INJECTION("حقن"),
    INTRAVENOUS("وريدي"),
    INTRAMUSCULAR("عضلي"),
    SUBCUTANEOUS("تحت الجلد"),
    INHALATION("استنشاق"),
    RECTAL("شرجي"),
    SUBLINGUAL("تحت اللسان"),
    NASAL("أنفي"),
    OPHTHALMIC("عيني"),
    OTIC("أذني");

    private final String arabicName;

    MedicationRoute(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
