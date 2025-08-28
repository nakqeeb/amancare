package com.nakqeeb.amancare.entity;

/**
 * نوع الجدولة
 */
public enum ScheduleType {
    REGULAR("جدول منتظم"),
    TEMPORARY("جدول مؤقت"),
    HOLIDAY_COVERAGE("تغطية إجازة"),
    EMERGENCY("طوارئ");

    private final String arabicName;

    ScheduleType(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() { return arabicName; }
}