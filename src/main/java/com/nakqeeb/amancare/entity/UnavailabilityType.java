package com.nakqeeb.amancare.entity;

/**
 * نوع عدم التوفر
 */
public enum UnavailabilityType {
    VACATION("إجازة"),
    SICK_LEAVE("إجازة مرضية"),
    CONFERENCE("مؤتمر"),
    TRAINING("تدريب"),
    PERSONAL("أمور شخصية"),
    EMERGENCY("طارئ"),
    OTHER("أخرى");

    private final String arabicName;

    UnavailabilityType(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() { return arabicName; }
}