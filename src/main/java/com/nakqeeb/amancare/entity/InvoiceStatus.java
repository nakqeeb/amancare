package com.nakqeeb.amancare.entity;

/**
 * حالة الفاتورة
 */
public enum InvoiceStatus {
    DRAFT("مسودة"),
    SENT("مرسلة"),
    PAID("مدفوعة"),
    PARTIALLY_PAID("مدفوعة جزئياً"),
    OVERDUE("متأخرة"),
    CANCELLED("ملغية");

    private final String arabicName;

    InvoiceStatus(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() { return arabicName; }
}