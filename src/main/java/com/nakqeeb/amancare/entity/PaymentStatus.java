package com.nakqeeb.amancare.entity;

/**
 * حالة الدفع
 * Payment Status
 */
public enum PaymentStatus {
    PENDING("معلق"),
    COMPLETED("مكتمل"),
    FAILED("فشل"),
    CANCELLED("ملغي"),
    REFUNDED("مسترد");

    private final String arabicName;

    PaymentStatus(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}
