// =============================================================================
// PDF Utilities - أدوات PDF المساعدة
// =============================================================================

package com.nakqeeb.amancare.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.Bidi;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * PDF Utility class for handling Arabic text and formatting
 * فئة مساعدة للتعامل مع النص العربي والتنسيق
 */
@Component
public class PdfUtils {

    private static final DateTimeFormatter AR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("ar"));
    private static final DateTimeFormatter AR_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("ar"));

    /**
     * Format date for Arabic display
     */
    public static String formatArabicDate(LocalDate date) {
        if (date == null) return "-";
        return date.format(AR_DATE);
    }

    /**
     * Format datetime for Arabic display
     */
    public static String formatArabicDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(AR_DATETIME);
    }

    /**
     * Ensure text is properly formatted for RTL
     */
    public static String prepareArabicText(String text) {
        if (text == null || text.isEmpty()) return "";

        // Remove any existing directional marks
        text = text.replaceAll("[\u200E\u200F\u202A-\u202E]", "");

        // Check if text contains Arabic characters
        if (containsArabic(text)) {
            // Add RTL mark at the beginning
            return "\u200F" + text;
        }

        return text;
    }

    /**
     * Check if text contains Arabic characters
     */
    public static boolean containsArabic(String text) {
        if (text == null) return false;
        for (char c : text.toCharArray()) {
            if (c >= 0x0600 && c <= 0x06FF || c >= 0x0750 && c <= 0x077F) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mix Arabic and English text properly
     */
    public static String mixArabicEnglish(String arabic, String english) {
        if (arabic == null || arabic.isEmpty()) return english;
        if (english == null || english.isEmpty()) return arabic;

        // Use zero-width joiner to separate properly
        return arabic + " \u200E(" + english + ")\u200F";
    }

    /**
     * Format phone numbers for RTL display
     */
    public static String formatPhoneRTL(String phone) {
        if (phone == null || phone.isEmpty()) return "-";

        // Add LTR mark for phone numbers
        return "\u200E" + phone + "\u200F";
    }

    /**
     * Load font from classpath
     */
    public static InputStream loadFont(String fontPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(fontPath);
        if (!resource.exists()) {
            // Try alternative paths
            resource = new ClassPathResource("fonts/" + fontPath);
            if (!resource.exists()) {
                resource = new ClassPathResource("static/fonts/" + fontPath);
            }
        }
        return resource.getInputStream();
    }

    /**
     * Get CSS for Arabic font
     */
    public static String getArabicFontCSS() {
        return """
            @font-face {
                font-family: 'Arabic';
                src: url('fonts/NotoSansArabic.ttf');
                font-weight: normal;
                font-style: normal;
            }
            
            * {
                font-family: 'Arabic', 'Noto Sans Arabic', 'Arial', sans-serif !important;
            }
            
            /* Ensure proper RTL rendering */
            body {
                direction: rtl;
                text-align: right;
                unicode-bidi: embed;
            }
            
            /* Fix for mixed content */
            .ltr {
                direction: ltr;
                text-align: left;
                unicode-bidi: embed;
            }
            
            /* Phone numbers and emails */
            .phone, .email {
                direction: ltr;
                text-align: right;
                unicode-bidi: plaintext;
            }
            """;
    }

    /**
     * Sanitize text for HTML/PDF output
     */
    public static String sanitizeForPdf(String text) {
        if (text == null) return "";

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Generate barcode/QR code content for patient
     */
    public static String generatePatientBarcode(String patientNumber) {
        return "PAT-" + patientNumber;
    }
}