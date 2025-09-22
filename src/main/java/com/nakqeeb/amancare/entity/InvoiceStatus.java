package com.nakqeeb.amancare.entity;

/**
 * حالة الفاتورة
 */
public enum InvoiceStatus {
    DRAFT("مسودة"),
    PENDING("معلقة"),
    SENT("مرسلة"),
    VIEWED("تمت المشاهدة"),
    PAID("مدفوعة"),
    PARTIALLY_PAID("مدفوعة جزئياً"),
    OVERDUE("متأخرة"),
    CANCELLED("ملغية"),
    REFUNDED("مستردة");

    private final String arabicName;

    InvoiceStatus(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}