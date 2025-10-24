package com.nakqeeb.amancare.entity;

/**
 * نوع تكوين مدة الموعد
 * Duration Configuration Type
 */
public enum DurationConfigType {
    /**
     * تحديد المدة مباشرة من قبل الموظفين
     * Direct duration setting by staff
     */
    DIRECT,

    /**
     * حساب المدة بناءً على عدد المواعيد المستهدف
     * Calculate duration based on target token count
     */
    TOKEN_BASED
}