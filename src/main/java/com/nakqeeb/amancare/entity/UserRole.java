package com.nakqeeb.amancare.entity;

/**
 * أدوار المستخدمين
 */
public enum UserRole {
    SYSTEM_ADMIN("مدير النظام"),
    ADMIN("مدير العيادة"),
    DOCTOR("طبيب"),
    NURSE("ممرض/ممرضة"),
    RECEPTIONIST("موظف استقبال");

    private final String arabicName;

    UserRole(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() { return arabicName; }
}