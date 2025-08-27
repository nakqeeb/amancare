package com.nakqeeb.amancare.entity;

public enum PaymentMethod {
    CASH("نقداً"),
    CARD("بطاقة ائتمانية"),
    BANK_TRANSFER("تحويل بنكي"),
    INSURANCE("تأمين صحي");

    private final String arabicName;

    PaymentMethod(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() { return arabicName; }
}