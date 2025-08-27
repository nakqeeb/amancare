package com.nakqeeb.amancare.entity;

/**
 * فصائل الدم
 */
public enum BloodType {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    private final String symbol;

    BloodType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() { return symbol; }

    @Override
    public String toString() {
        return this.symbol;
    }
}