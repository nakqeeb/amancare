// =============================================================================
// Enums - التعدادات
// =============================================================================

package com.nakqeeb.amancare.entity;

/**
 * خطط الاشتراك
 */
public enum SubscriptionPlan {
    BASIC("أساسي"),
    PREMIUM("مميز"),
    ENTERPRISE("مؤسسي");

    private final String arabicName;

    SubscriptionPlan(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() { return arabicName; }
}
