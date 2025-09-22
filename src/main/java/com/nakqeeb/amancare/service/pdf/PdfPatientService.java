package com.nakqeeb.amancare.service.pdf;

import com.nakqeeb.amancare.dto.response.PatientResponse;
import com.nakqeeb.amancare.entity.Patient;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * خدمة توليد ملفات PDF
 * Enhanced for better OpenHTMLtoPDF compatibility
 */
@Service
public class PdfPatientService {
    private static final Logger logger = LoggerFactory.getLogger(PdfPatientService.class);

    @Autowired
    private TemplateEngine templateEngine;

    private final DateTimeFormatter arabicDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("ar"));
    private final DateTimeFormatter arabicDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("ar"));

    /**
     * Generate PDF for patient details
     * توليد PDF لتفاصيل المريض
     */
    public byte[] generatePatientDetailsPdf(PatientResponse patient) {
        logger.info("Generating PDF for patient: {}", patient.getPatientNumber());

        try {
            // Prepare template data
            Context context = new Context(new Locale("ar"));
            Map<String, Object> variables = preparePatientVariables(patient);
            context.setVariables(variables);

            // Process HTML template with Thymeleaf
            String htmlContent = templateEngine.process("pdf/patient-details", context);

            // Clean and prepare HTML for OpenHTMLtoPDF
            htmlContent = preprocessHtml(htmlContent);

            // Generate PDF with Arabic support
            return generatePdfFromHtml(htmlContent);

        } catch (Exception e) {
            logger.error("Error generating patient PDF: ", e);
            throw new RuntimeException("فشل في توليد ملف PDF للمريض");
        }
    }

    /**
     * Generate PDF for patient summary/card
     * توليد بطاقة المريض
     */
    public byte[] generatePatientCardPdf(PatientResponse patient) {
        logger.info("Generating patient card PDF for: {}", patient.getPatientNumber());

        try {
            Context context = new Context(new Locale("ar"));
            Map<String, Object> variables = preparePatientVariables(patient);
            context.setVariables(variables);

            String htmlContent = templateEngine.process("pdf/patient-card", context);
            htmlContent = preprocessHtml(htmlContent);

            return generatePdfFromHtml(htmlContent);

        } catch (Exception e) {
            logger.error("Error generating patient card PDF: ", e);
            throw new RuntimeException("فشل في توليد بطاقة المريض");
        }
    }

    /**
     * Preprocess HTML to ensure OpenHTMLtoPDF compatibility
     */
    private String preprocessHtml(String html) {
        // Parse HTML with JSoup to clean it
        Document doc = Jsoup.parse(html);

        // Ensure all images have absolute paths or are base64
        doc.select("img").forEach(img -> {
            String src = img.attr("src");
            if (!src.startsWith("data:") && !src.startsWith("http")) {
                // Convert relative paths to absolute or base64
                img.attr("src", convertToBase64Image(src));
            }
        });

        // Remove any unsupported CSS properties
        doc.select("*[style]").forEach(element -> {
            String style = element.attr("style");
            style = cleanCssStyle(style);
            element.attr("style", style);
        });

        return doc.html();
    }

    /**
     * Clean CSS style to remove unsupported properties
     */
    private String cleanCssStyle(String style) {
        if (style == null || style.isEmpty()) return "";

        // Remove unsupported CSS properties
        style = style.replaceAll("display\\s*:\\s*flex[^;]*;?", "");
        style = style.replaceAll("display\\s*:\\s*grid[^;]*;?", "");
        style = style.replaceAll("gap\\s*:[^;]*;?", "");
        style = style.replaceAll("backdrop-filter\\s*:[^;]*;?", "");
        style = style.replaceAll("filter\\s*:[^;]*;?", "");
        style = style.replaceAll("transform\\s*:[^;]*;?", "");
        style = style.replaceAll("transition\\s*:[^;]*;?", "");
        style = style.replaceAll("animation[^;]*;?", "");

        // Remove opacity property
        style = style.replaceAll("opacity\\s*:[^;]*;?", "");

        // Convert rgba colors to hex (simplified conversion)
        style = convertRgbaToHex(style);

        return style.trim();
    }

//    /**
//     * Convert RGBA colors to hex colors (approximate)
//     */
//    private String convertRgbaToHex(String style) {
//        // Pattern to match rgba colors
//        String rgbaPattern = "rgba?\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*(?:,\\s*([\\d.]+))?\\s*\\)";
//
//        // Replace rgba with hex approximations
//        style = style.replaceAll(rgbaPattern, (match) -> {
//            // For simplicity, return a fallback color
//            // In production, you might want more sophisticated color mixing
//            if (match.getPlainText().contains("255")) {
//                return "#ffffff"; // White fallback
//            } else if (match.getPlainText().contains("0,")) {
//                return "#000000"; // Black fallback
//            } else {
//                return "#808080"; // Gray fallback
//            }
//        });
//
//        // Simpler approach - just replace common rgba patterns
//        style = style.replaceAll("rgba\\([^)]+\\)", "#8896e8"); // Default to light purple
//        style = style.replaceAll("rgb\\([^)]+\\)", "#667eea"); // Default to purple
//
//        return style;
//    }

    /**
     * Convert RGBA or RGB colors in CSS to hex colors.
     */
    private String convertRgbaToHex(String style) {
        String rgbaPattern = "rgba?\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)(?:\\s*,\\s*([\\d.]+))?\\s*\\)";
        Pattern pattern = Pattern.compile(rgbaPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(style);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            int r = Integer.parseInt(matcher.group(1));
            int g = Integer.parseInt(matcher.group(2));
            int b = Integer.parseInt(matcher.group(3));
            // Optional alpha, ignored for hex conversion
            // String alpha = matcher.group(4);

            // Convert to hex
            String replacement = String.format("#%02x%02x%02x", r, g, b);

            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Convert image path to base64
     */
    private String convertToBase64Image(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                return getBase64Logo(); // Return default logo
            }
            byte[] imageBytes = IOUtils.toByteArray(resource.getInputStream());
            String mimeType = detectMimeType(path);
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            logger.warn("Could not convert image to base64: {}", path);
            return getBase64Logo();
        }
    }

    /**
     * Detect MIME type from file extension
     */
    private String detectMimeType(String filename) {
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".gif")) return "image/gif";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        return "image/png"; // Default
    }

    /**
     * Prepare patient data for template
     */
    private Map<String, Object> preparePatientVariables(PatientResponse patient) {
        Map<String, Object> variables = new HashMap<>();

        // Basic patient info
        variables.put("patient", patient);
        variables.put("patientNumber", patient.getPatientNumber() != null ? patient.getPatientNumber() : "");
        variables.put("fullName", patient.getFullName() != null ? patient.getFullName() : "");
        variables.put("firstName", patient.getFirstName() != null ? patient.getFirstName() : "");
        variables.put("lastName", patient.getLastName() != null ? patient.getLastName() : "");

        // Format dates
        if (patient.getDateOfBirth() != null) {
            variables.put("dateOfBirth", arabicDateFormatter.format(patient.getDateOfBirth()));
        } else {
            variables.put("dateOfBirth", "-");
        }

        // Age
        variables.put("age", patient.getAge() != null ? patient.getAge() + " سنة" : "-");

        // Gender in Arabic
        variables.put("genderArabic", translateGender(patient.getGender().toString()));

        // Blood type in Arabic
        variables.put("bloodTypeArabic", translateBloodType(patient.getBloodType().toString()));

        // Contact info
        variables.put("phone", patient.getPhone() != null ? patient.getPhone() : "-");
        variables.put("email", patient.getEmail() != null ? patient.getEmail() : "-");
        variables.put("address", patient.getAddress() != null ? patient.getAddress() : "-");

        // Emergency contact
        variables.put("emergencyContactName",
                patient.getEmergencyContactName() != null ? patient.getEmergencyContactName() : "-");
        variables.put("emergencyContactPhone",
                patient.getEmergencyContactPhone() != null ? patient.getEmergencyContactPhone() : "-");

        // Medical info
        variables.put("allergies", patient.getAllergies() != null ? patient.getAllergies() : "لا يوجد");
        variables.put("chronicDiseases", patient.getChronicDiseases() != null ? patient.getChronicDiseases() : "لا يوجد");
        variables.put("notes", patient.getNotes() != null ? patient.getNotes() : "-");

        // System info
        variables.put("currentDate", arabicDateFormatter.format(LocalDate.now()));
        variables.put("currentDateTime", arabicDateTimeFormatter.format(LocalDateTime.now()));

        // Company branding
        variables.put("companyName", "أمان كير");
        variables.put("companyNameEn", "Amancare");
        variables.put("companyLogo", getBase64Logo());

        return variables;
    }

    /**
     * Generate PDF from HTML content with Arabic support
     */
    private byte[] generatePdfFromHtml(String htmlContent) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();

        // Configure builder with proper settings
        builder.useFastMode(); // Better performance for simpler CSS
        builder.testMode(false);
        builder.usePdfUaAccessbility(false); // Disable if causing issues
        builder.usePdfAConformance(PdfRendererBuilder.PdfAConformance.NONE);

        // Set HTML content using W3C DOM to avoid issues
        Document jsoupDoc = Jsoup.parse(htmlContent, "UTF-8");
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);
        builder.withW3cDocument(w3cDoc, null);

        // Configure for Arabic/RTL support
        builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
        builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
        builder.defaultTextDirection(PdfRendererBuilder.TextDirection.RTL);

        // Add fonts with proper configuration
        addFonts(builder);

        // Output to byte array
        builder.toStream(outputStream);

        // Build PDF
        builder.run();

        return outputStream.toByteArray();
    }

    /**
     * Add fonts to PDF builder
     */
    private void addFonts(PdfRendererBuilder builder) {
        try {
            // Primary Arabic font
            InputStream arabicFont = loadFont("NotoSansArabic-Regular.ttf");
            if (arabicFont != null) {
                builder.useFont(() -> arabicFont, "Noto Sans Arabic", 400,
                        BaseRendererBuilder.FontStyle.NORMAL, true);
            }

            // Fallback font - Amiri Unicode or similar
            InputStream fallbackFont = loadFont("Amiri.ttf");
            if (fallbackFont != null) {
                builder.useFont(() -> fallbackFont, "Amiri", 400,
                        BaseRendererBuilder.FontStyle.NORMAL, true);
            }

        } catch (Exception e) {
            logger.error("Error loading fonts: ", e);
            // Continue without custom fonts - will use embedded fonts
        }
    }

    /**
     * Load font from various possible locations
     */
    private InputStream loadFont(String fontName) {
        String[] paths = {
                "fonts/" + fontName,
                "static/fonts/" + fontName,
                fontName
        };

        for (String path : paths) {
            try {
                ClassPathResource resource = new ClassPathResource(path);
                if (resource.exists()) {
                    return resource.getInputStream();
                }
            } catch (Exception e) {
                // Try next path
            }
        }

        logger.warn("Font not found: {}", fontName);
        return null;
    }

    /**
     * Get company logo as Base64
     */
    private String getBase64Logo() {
        try {
            InputStream logoStream = new ClassPathResource("images/amancare-logo.png").getInputStream();
            byte[] logoBytes = IOUtils.toByteArray(logoStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(logoBytes);
        } catch (IOException e) {
            logger.warn("Could not load company logo: ", e);
            // Return a simple SVG logo as fallback
            return generateSvgLogo();
        }
    }

    /**
     * Generate SVG logo as fallback
     */
    private String generateSvgLogo() {
        String svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="200" height="60" viewBox="0 0 200 60">
                <rect width="200" height="60" fill="#2c3e50" rx="5"/>
                <text x="100" y="35" font-family="Amiri, sans-serif" font-size="24" font-weight="bold" 
                      text-anchor="middle" fill="white">أمان كير</text>
                <text x="100" y="50" font-family="Amiri, sans-serif" font-size="12" 
                      text-anchor="middle" fill="#3498db">Amancare</text>
            </svg>
            """;
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes());
    }

    /**
     * Translate gender to Arabic
     */
    private String translateGender(String gender) {
        if (gender == null) return "-";
        return switch (gender.toUpperCase()) {
            case "MALE" -> "ذكر";
            case "FEMALE" -> "أنثى";
            default -> gender;
        };
    }

    /**
     * Translate blood type to Arabic
     */
    private String translateBloodType(String bloodType) {
        if (bloodType == null) return "-";
        return switch (bloodType) {
            case "A_POSITIVE" -> "A+";
            case "A_NEGATIVE" -> "A-";
            case "B_POSITIVE" -> "B+";
            case "B_NEGATIVE" -> "B-";
            case "AB_POSITIVE" -> "AB+";
            case "AB_NEGATIVE" -> "AB-";
            case "O_POSITIVE" -> "O+";
            case "O_NEGATIVE" -> "O-";
            default -> bloodType;
        };
    }
}