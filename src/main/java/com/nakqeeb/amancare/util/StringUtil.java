// =============================================================================
// String Utilities - أدوات النصوص
// =============================================================================

package com.nakqeeb.amancare.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * أدوات مساعدة للنصوص
 */
public class StringUtil {

    /**
     * تنظيف النص وإزالة المسافات الزائدة
     */
    public static String clean(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * دمج النصوص بمسافة
     */
    public static String joinWithSpace(String... texts) {
        return Arrays.stream(texts)
                .filter(text -> text != null && !text.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    /**
     * تحويل النص إلى أحرف صغيرة لمقارنة البحث
     */
    public static String normalizeForSearch(String text) {
        if (text == null) return "";

        // إزالة الشدة والتشكيل من النص العربي
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.toLowerCase()
                .replaceAll("[\u064B-\u065F\u0670\u0640]", "") // إزالة التشكيل
                .trim();
    }

    /**
     * إخفاء جزء من رقم الهاتف للخصوصية
     */
    public static String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }

        int visibleLength = 3;
        String visible = phone.substring(0, visibleLength);
        String masked = "*".repeat(phone.length() - visibleLength * 2);
        String ending = phone.substring(phone.length() - visibleLength);

        return visible + masked + ending;
    }

    /**
     * اقتطاع النص إلى طول محدد
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * التحقق من احتواء النص على كلمة البحث
     */
    public static boolean containsIgnoreCase(String text, String searchTerm) {
        if (text == null || searchTerm == null) {
            return false;
        }
        return normalizeForSearch(text).contains(normalizeForSearch(searchTerm));
    }
}