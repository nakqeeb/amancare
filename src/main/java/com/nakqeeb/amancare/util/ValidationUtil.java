// =============================================================================
// Validation Utils - أدوات التحقق
// =============================================================================

package com.nakqeeb.amancare.util;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * أدوات مساعدة للتحقق من البيانات
 */
public class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{9,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    /**
     * التحقق من صحة رقم الهاتف اليمني
     */
    public static boolean isValidYemeniPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }

        // إزالة المسافات والرموز الخاصة
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");

        // التحقق من الأرقام اليمنية (تبدأ بـ 77، 71، 73، 70)
        return cleanPhone.matches("^(78|77|71|73|70)\\d{7}$") ||
                cleanPhone.matches("^(\\+967|00967)(78|77|71|73|70)\\d{7}$");
    }

    /**
     * التحقق من صحة البريد الإلكتروني
     */
    public static boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * التحقق من صحة العمر
     */
    public static boolean isValidAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return true; // تاريخ الميلاد اختياري
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age >= 0 && age <= 120;
    }

    /**
     * التحقق من قوة كلمة المرور
     */
    public static boolean isStrongPassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 8) {
            return false;
        }

        // يجب أن تحتوي على حرف كبير وصغير ورقم
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasUpperCase && hasLowerCase && hasDigit;
    }

    /**
     * تنظيف النص من المسافات الزائدة
     */
    public static String cleanText(String text) {
        return StringUtils.hasText(text) ? text.trim().replaceAll("\\s+", " ") : null;
    }

    /**
     * التحقق من صحة اسم المريض
     */
    public static boolean isValidName(String name) {
        if (!StringUtils.hasText(name)) {
            return false;
        }

        String cleanName = name.trim();
        return cleanName.length() >= 2 && cleanName.length() <= 100 &&
                cleanName.matches("^[\\u0600-\\u06FF\\u0750-\\u077F\\s\\-\\.a-zA-Z]+$");
    }
}