package com.nakqeeb.amancare.entity;

/**
 * طريقة الدفع
 * Payment Method
 */
public enum PaymentMethod {
    CASH("نقدي"),
    CREDIT_CARD("بطاقة ائتمان"),
    DEBIT_CARD("بطاقة مدين"),
    BANK_TRANSFER("حوالة بنكية"),
    CHECK("شيك"),
    INSURANCE("تأمين"),
    INSTALLMENT("تقسيط"),
    ONLINE("دفع إلكتروني");

    private final String arabicName;

    PaymentMethod(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}