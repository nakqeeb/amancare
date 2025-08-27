package com.nakqeeb.amancare.entity;

/**
 * نوع الموعد
 */
public enum AppointmentType {
    CONSULTATION("استشارة"),
    FOLLOW_UP("متابعة"),
    EMERGENCY("طارئ");

    private final String arabicName;

    AppointmentType(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() { return arabicName; }
}
