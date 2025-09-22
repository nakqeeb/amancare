package com.nakqeeb.amancare.entity;

/**
 * فئة الخدمة الطبية
 * Medical Service Category
 */
public enum ServiceCategory {
    CONSULTATION("استشارة"),
    PROCEDURE("إجراء طبي"),
    MEDICATION("دواء"),
    LAB_TEST("فحص مختبر"),
    RADIOLOGY("أشعة"),
    SURGERY("عملية جراحية"),
    THERAPY("علاج طبيعي"),
    VACCINATION("تطعيم"),
    EQUIPMENT("معدات"),
    OTHER("أخرى");

    private final String arabicName;

    ServiceCategory(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}