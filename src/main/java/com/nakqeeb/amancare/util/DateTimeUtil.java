// =============================================================================
// Date Time Utilities - أدوات التاريخ والوقت
// =============================================================================

package com.nakqeeb.amancare.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * أدوات مساعدة للتاريخ والوقت
 */
public class DateTimeUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter ARABIC_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * تحويل التاريخ إلى نص بالتنسيق العربي
     */
    public static String formatArabicDate(LocalDate date) {
        return date != null ? date.format(ARABIC_DATE_FORMATTER) : "";
    }

    /**
     * تحويل التاريخ والوقت إلى نص
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "";
    }

    /**
     * التحقق من صحة صيغة التاريخ
     */
    public static boolean isValidDateFormat(String dateString) {
        try {
            LocalDate.parse(dateString, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * الحصول على بداية اليوم
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * الحصول على نهاية اليوم
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(23, 59, 59);
    }

    /**
     * حساب العمر بالسنوات
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * التحقق من أن التاريخ في المستقبل
     */
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * التحقق من أن التاريخ في الماضي
     */
    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }
}