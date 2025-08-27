package com.nakqeeb.amancare.entity;

/**
 * حالة الموعد
 */
public enum AppointmentStatus {
    SCHEDULED("مجدول"),
    CONFIRMED("مؤكد"),
    IN_PROGRESS("قيد التنفيذ"),
    COMPLETED("مكتمل"),
    CANCELLED("ملغي"),
    NO_SHOW("لم يحضر");

    private final String arabicName;

    AppointmentStatus(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() { return arabicName; }
}