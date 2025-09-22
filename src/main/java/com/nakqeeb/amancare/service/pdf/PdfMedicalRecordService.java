package com.nakqeeb.amancare.service.pdf;

import com.nakqeeb.amancare.dto.response.healthrecords.*;
import com.nakqeeb.amancare.entity.healthrecords.*;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * خدمة توليد ملفات PDF
 * Enhanced for better OpenHTMLtoPDF compatibility
 */
@Service
public class PdfMedicalRecordService {
    private static final Logger logger = LoggerFactory.getLogger(PdfMedicalRecordService.class);

    @Autowired
    private TemplateEngine templateEngine;

    private final DateTimeFormatter arabicDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("ar"));
    private final DateTimeFormatter arabicDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("ar"));

    /**
     * إنشاء ملف PDF للسجل الطبي
     * Generate PDF for medical record
     */
    public byte[] generateMedicalRecordPdf(MedicalRecordResponse medicalRecord) throws IOException {
        logger.info("إنشاء ملف PDF للسجل الطبي: {}", medicalRecord.getId());

        try {
            // Prepare template data
            Context context = new Context(new Locale("ar"));
            Map<String, Object> variables = prepareMedicalRecordContext(medicalRecord);
            context.setVariables(variables);

            // Process HTML template with Thymeleaf
            String htmlContent = templateEngine.process("pdf/medical-record-template", context);

            // Clean and prepare HTML for OpenHTMLtoPDF
            htmlContent = preprocessHtml(htmlContent);

            // Generate PDF with Arabic support
            return generatePdfFromHtml(htmlContent);

        } catch (Exception e) {
            logger.error("خطأ في إنشاء ملف PDF للسجل الطبي {}: ", medicalRecord.getId(), e);
            throw new IOException("فشل في إنشاء ملف PDF للسجل الطبي", e);
        }
    }

    /**
     * إنشاء ملف PDF لتقرير السجلات الطبية المتعددة
     * Generate PDF for multiple medical records report
     */
    public byte[] generateMedicalRecordsReportPdf(
            List<MedicalRecordResponse> medicalRecords,
            String reportTitle,
            String clinicName,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate) throws IOException {

        logger.info("إنشاء تقرير PDF لـ {} سجل طبي", medicalRecords.size());

        try {
            Context context = new Context();
            context.setVariables(prepareMedicalRecordsReportContext(medicalRecords, reportTitle, clinicName, fromDate, toDate));

            String htmlContent = templateEngine.process("pdf/medical-records-report-template", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/NotoSansArabic-Regular.ttf"), "NotoSansArabic");
            builder.run();

            logger.info("تم إنشاء تقرير PDF للسجلات الطبية بنجاح");
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("خطأ في إنشاء تقرير PDF للسجلات الطبية: ", e);
            throw new IOException("فشل في إنشاء تقرير PDF للسجلات الطبية", e);
        }
    }

    /**
     * إنشاء ملف PDF لوصفة طبية
     * Generate PDF for medical prescription
     */
    public byte[] generatePrescriptionPdf(MedicalRecordResponse medicalRecord) throws IOException {
        logger.info("إنشاء ملف PDF للوصفة الطبية من السجل: {}", medicalRecord.getId());

        if (medicalRecord.getPrescriptions() == null || medicalRecord.getPrescriptions().isEmpty()) {
            throw new IllegalArgumentException("لا توجد وصفات طبية في هذا السجل");
        }

        try {
            Context context = new Context();
            context.setVariables(preparePrescriptionContext(medicalRecord));

            String htmlContent = templateEngine.process("pdf/prescription-template", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/NotoSansArabic-Regular.ttf"), "NotoSansArabic");
            builder.run();

            logger.info("تم إنشاء ملف PDF للوصفة الطبية بنجاح");
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("خطأ في إنشاء ملف PDF للوصفة الطبية: ", e);
            throw new IOException("فشل في إنشاء ملف PDF للوصفة الطبية", e);
        }
    }

    /**
     * إنشاء ملف PDF لتقرير الفحوصات المخبرية
     * Generate PDF for lab tests report
     */
    public byte[] generateLabTestsReportPdf(MedicalRecordResponse medicalRecord) throws IOException {
        logger.info("إنشاء ملف PDF لتقرير الفحوصات المخبرية من السجل: {}", medicalRecord.getId());

        if (medicalRecord.getLabTests() == null || medicalRecord.getLabTests().isEmpty()) {
            throw new IllegalArgumentException("لا توجد فحوصات مخبرية في هذا السجل");
        }

        try {
            Context context = new Context();
            context.setVariables(prepareLabTestsContext(medicalRecord));

            String htmlContent = templateEngine.process("pdf/lab-tests-template", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(outputStream);
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/NotoSansArabic-Regular.ttf"), "NotoSansArabic");
            builder.run();

            logger.info("تم إنشاء ملف PDF لتقرير الفحوصات المخبرية بنجاح");
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("خطأ في إنشاء ملف PDF لتقرير الفحوصات المخبرية: ", e);
            throw new IOException("فشل في إنشاء ملف PDF لتقرير الفحوصات المخبرية", e);
        }
    }

    // =============================================================================
    // PRIVATE HELPER METHODS
    // =============================================================================

    /**
     * تحضير سياق قالب السجل الطبي
     * Prepare medical record template context
     */
    private Map<String, Object> prepareMedicalRecordContext(MedicalRecordResponse medicalRecord) {
        Map<String, Object> context = new HashMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Basic information
        context.put("medicalRecord", medicalRecord);
        context.put("currentDate", java.time.LocalDate.now().format(dateFormatter));
        context.put("currentDateTime", java.time.LocalDateTime.now().format(dateTimeFormatter));

        // Formatted dates
        if (medicalRecord.getVisitDate() != null) {
            context.put("formattedVisitDate", medicalRecord.getVisitDate().format(dateFormatter));
        }

        if (medicalRecord.getFollowUpDate() != null) {
            context.put("formattedFollowUpDate", medicalRecord.getFollowUpDate().format(dateFormatter));
        }

        // Vital signs formatted
        if (medicalRecord.getVitalSigns() != null) {
            context.put("vitalSigns", medicalRecord.getVitalSigns());
            context.put("bloodPressure", formatBloodPressure(medicalRecord.getVitalSigns()));
            context.put("bmiCategory", getBMICategory(medicalRecord.getVitalSigns().getBmi()));
        }

        // Primary diagnosis
        if (medicalRecord.getDiagnosis() != null) {
            medicalRecord.getDiagnosis().stream()
                    .filter(d -> d.getIsPrimary())
                    .findFirst()
                    .ifPresent(d -> context.put("primaryDiagnosis", d));
        }

        // Counts for summary
        context.put("diagnosisCount", medicalRecord.getDiagnosis() != null ? medicalRecord.getDiagnosis().size() : 0);
        context.put("prescriptionCount", medicalRecord.getPrescriptions() != null ? medicalRecord.getPrescriptions().size() : 0);
        context.put("labTestCount", medicalRecord.getLabTests() != null ? medicalRecord.getLabTests().size() : 0);
        context.put("radiologyTestCount", medicalRecord.getRadiologyTests() != null ? medicalRecord.getRadiologyTests().size() : 0);
        context.put("procedureCount", medicalRecord.getProcedures() != null ? medicalRecord.getProcedures().size() : 0);
        context.put("referralCount", medicalRecord.getReferrals() != null ? medicalRecord.getReferrals().size() : 0);

        // Additional formatting helpers
        context.put("hasVitalSigns", medicalRecord.getVitalSigns() != null);
        context.put("hasPrescriptions", medicalRecord.getPrescriptions() != null && !medicalRecord.getPrescriptions().isEmpty());
        context.put("hasLabTests", medicalRecord.getLabTests() != null && !medicalRecord.getLabTests().isEmpty());
        context.put("hasRadiologyTests", medicalRecord.getRadiologyTests() != null && !medicalRecord.getRadiologyTests().isEmpty());
        context.put("hasProcedures", medicalRecord.getProcedures() != null && !medicalRecord.getProcedures().isEmpty());
        context.put("hasReferrals", medicalRecord.getReferrals() != null && !medicalRecord.getReferrals().isEmpty());

        return context;
    }

    /**
     * تحضير سياق قالب تقرير السجلات الطبية المتعددة
     * Prepare medical records report template context
     */
    private Map<String, Object> prepareMedicalRecordsReportContext(
            List<MedicalRecordResponse> medicalRecords,
            String reportTitle,
            String clinicName,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate) {

        Map<String, Object> context = new HashMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Report metadata
        context.put("reportTitle", reportTitle != null ? reportTitle : "تقرير السجلات الطبية");
        context.put("clinicName", clinicName != null ? clinicName : "أمان كير");
        context.put("currentDate", java.time.LocalDate.now().format(dateFormatter));
        context.put("currentDateTime", java.time.LocalDateTime.now().format(dateTimeFormatter));
        context.put("fromDate", fromDate != null ? fromDate.format(dateFormatter) : "غير محدد");
        context.put("toDate", toDate != null ? toDate.format(dateFormatter) : "غير محدد");

        // Records data
        context.put("medicalRecords", medicalRecords);
        context.put("totalRecords", medicalRecords.size());

        // Statistics
        Map<String, Long> visitTypeStats = medicalRecords.stream()
                .collect(Collectors.groupingBy(
                        mr -> mr.getVisitTypeArabic() != null ? mr.getVisitTypeArabic() : "غير محدد",
                        Collectors.counting()));
        context.put("visitTypeStats", visitTypeStats);

        Map<String, Long> statusStats = medicalRecords.stream()
                .collect(Collectors.groupingBy(
                        mr -> mr.getStatusArabic() != null ? mr.getStatusArabic() : "غير محدد",
                        Collectors.counting()));
        context.put("statusStats", statusStats);

        // Doctor statistics
        Map<String, Long> doctorStats = medicalRecords.stream()
                .collect(Collectors.groupingBy(
                        mr -> mr.getDoctorName() != null ? mr.getDoctorName() : "غير محدد",
                        Collectors.counting()));
        context.put("doctorStats", doctorStats);

        return context;
    }

    /**
     * تحضير سياق قالب الوصفة الطبية
     * Prepare prescription template context
     */
    private Map<String, Object> preparePrescriptionContext(MedicalRecordResponse medicalRecord) {
        Map<String, Object> context = new HashMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Basic information
        context.put("medicalRecord", medicalRecord);
        context.put("prescriptions", medicalRecord.getPrescriptions());
        context.put("currentDate", java.time.LocalDate.now().format(dateFormatter));

        if (medicalRecord.getVisitDate() != null) {
            context.put("formattedVisitDate", medicalRecord.getVisitDate().format(dateFormatter));
        }

        // Primary diagnosis for prescription context
        if (medicalRecord.getDiagnosis() != null) {
            medicalRecord.getDiagnosis().stream()
                    .filter(d -> d.getIsPrimary())
                    .findFirst()
                    .ifPresent(d -> context.put("primaryDiagnosis", d.getDescription()));
        }

        // Prescription summary
        context.put("totalMedications", medicalRecord.getPrescriptions().size());

        // Group prescriptions by route
        Map<String, List<PrescriptionResponse>> prescriptionsByRoute =
                medicalRecord.getPrescriptions().stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getRouteArabic() != null ? p.getRouteArabic() : "غير محدد"));
        context.put("prescriptionsByRoute", prescriptionsByRoute);

        return context;
    }

    /**
     * تحضير سياق قالب الفحوصات المخبرية
     * Prepare lab tests template context
     */
    private Map<String, Object> prepareLabTestsContext(MedicalRecordResponse medicalRecord) {
        Map<String, Object> context = new HashMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Basic information
        context.put("medicalRecord", medicalRecord);
        context.put("labTests", medicalRecord.getLabTests());
        context.put("currentDate", java.time.LocalDate.now().format(dateFormatter));

        if (medicalRecord.getVisitDate() != null) {
            context.put("formattedVisitDate", medicalRecord.getVisitDate().format(dateFormatter));
        }

        // Lab tests summary
        context.put("totalLabTests", medicalRecord.getLabTests().size());

        // Group by category
        Map<String, List<LabTestResponse>> testsByCategory =
                medicalRecord.getLabTests().stream()
                        .collect(Collectors.groupingBy(
                                t -> t.getCategoryArabic() != null ? t.getCategoryArabic() : "غير محدد"));
        context.put("testsByCategory", testsByCategory);

        // Group by status
        Map<String, List<LabTestResponse>> testsByStatus =
                medicalRecord.getLabTests().stream()
                        .collect(Collectors.groupingBy(
                                t -> t.getStatusArabic() != null ? t.getStatusArabic() : "غير محدد"));
        context.put("testsByStatus", testsByStatus);

        // Statistics
        long completedTests = medicalRecord.getLabTests().stream()
                .filter(t -> "مكتمل".equals(t.getStatusArabic()))
                .count();
        context.put("completedTestsCount", completedTests);

        long pendingTests = medicalRecord.getLabTests().stream()
                .filter(t -> !"مكتمل".equals(t.getStatusArabic()))
                .count();
        context.put("pendingTestsCount", pendingTests);

        return context;
    }

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================

    /**
     * تنسيق ضغط الدم
     * Format blood pressure
     */
    private String formatBloodPressure(VitalSignsResponse vitalSigns) {
        if (vitalSigns.getBloodPressureSystolic() != null && vitalSigns.getBloodPressureDiastolic() != null) {
            return vitalSigns.getBloodPressureSystolic() + "/" + vitalSigns.getBloodPressureDiastolic() + " mmHg";
        }
        return "غير مسجل";
    }

    /**
     * تصنيف مؤشر كتلة الجسم
     * Get BMI category
     */
    private String getBMICategory(java.math.BigDecimal bmi) {
        if (bmi == null) return "غير محدد";

        double bmiValue = bmi.doubleValue();
        if (bmiValue < 18.5) return "نقص في الوزن";
        if (bmiValue < 25) return "وزن طبيعي";
        if (bmiValue < 30) return "زيادة في الوزن";
        if (bmiValue < 35) return "سمنة من الدرجة الأولى";
        if (bmiValue < 40) return "سمنة من الدرجة الثانية";
        return "سمنة مفرطة";
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


}