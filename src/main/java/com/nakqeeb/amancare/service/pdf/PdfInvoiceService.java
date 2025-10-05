// =============================================================================
// PDF Invoice Service - خدمة توليد فواتير PDF
// src/main/java/com/nakqeeb/amancare/service/pdf/PdfInvoiceService.java
// =============================================================================

package com.nakqeeb.amancare.service.pdf;

import com.nakqeeb.amancare.dto.response.InvoiceResponse;
import com.nakqeeb.amancare.dto.response.InvoiceItemResponse;
import com.nakqeeb.amancare.dto.response.PaymentResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import org.apache.commons.io.IOUtils;
import org.jsoup.helper.W3CDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * خدمة توليد ملفات PDF للفواتير
 * Invoice PDF Generation Service with Arabic support
 */
@Service
public class PdfInvoiceService {
    private static final Logger logger = LoggerFactory.getLogger(PdfInvoiceService.class);

    @Autowired
    private TemplateEngine templateEngine;

    private final DateTimeFormatter arabicDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("ar"));
    private final DateTimeFormatter arabicDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("ar"));

    /**
     * Generate PDF for invoice
     * إنشاء ملف PDF للفاتورة
     */
    public byte[] generateInvoicePdf(InvoiceResponse invoice) throws IOException {
        logger.info("إنشاء ملف PDF للفاتورة رقم: {}", invoice.getInvoiceNumber());

        try {
            // Prepare template data
            Context context = new Context(new Locale("ar"));
            Map<String, Object> variables = prepareInvoiceContext(invoice);
            context.setVariables(variables);

            // Process HTML template with Thymeleaf
            String htmlContent = templateEngine.process("pdf/invoice-template", context);

            // Clean and prepare HTML for OpenHTMLtoPDF
            htmlContent = preprocessHtml(htmlContent);

            // Generate PDF with Arabic support
            return generatePdfFromHtml(htmlContent);

        } catch (Exception e) {
            logger.error("خطأ في إنشاء ملف PDF للفاتورة {}: ", invoice.getInvoiceNumber(), e);
            throw new IOException("فشل في إنشاء ملف PDF للفاتورة", e);
        }
    }

    /**
     * Generate PDF for invoice receipt (simplified version)
     * إنشاء ملف PDF لإيصال الدفع
     */
    public byte[] generateInvoiceReceiptPdf(InvoiceResponse invoice) throws IOException {
        logger.info("إنشاء إيصال دفع PDF للفاتورة: {}", invoice.getInvoiceNumber());

        try {
            Context context = new Context(new Locale("ar"));
            Map<String, Object> variables = prepareInvoiceContext(invoice);
            context.setVariables(variables);

            String htmlContent = templateEngine.process("pdf/invoice-receipt-template", context);
            htmlContent = preprocessHtml(htmlContent);

            return generatePdfFromHtml(htmlContent);

        } catch (Exception e) {
            logger.error("خطأ في إنشاء إيصال PDF للفاتورة {}: ", invoice.getInvoiceNumber(), e);
            throw new IOException("فشل في إنشاء إيصال PDF", e);
        }
    }

    /**
     * Prepare invoice context for template
     */
    private Map<String, Object> prepareInvoiceContext(InvoiceResponse invoice) {
        Map<String, Object> variables = new HashMap<>();

        // Basic invoice info
        variables.put("invoice", invoice);
        variables.put("invoiceNumber", invoice.getInvoiceNumber());
        variables.put("invoiceDate", formatDate(invoice.getInvoiceDate()));
        variables.put("dueDate", formatDate(invoice.getDueDate()));
        variables.put("currentDate", formatDate(LocalDate.now()));

        // Patient info
        variables.put("patientName", invoice.getPatientName());
        variables.put("patientPhone", invoice.getPatientPhone() != null ? invoice.getPatientPhone() : "-");

        // Clinic info
        variables.put("clinicName", invoice.getClinicName() != null ? invoice.getClinicName() : "أمان كير");

        // Status info
        variables.put("statusLabel", getStatusLabel(invoice.getStatus()));
        variables.put("paymentStatusLabel", getPaymentStatusLabel(invoice.getPaymentStatus()));
        variables.put("statusColor", getStatusColor(invoice.getStatus()));

        // Financial details
        variables.put("subtotal", formatCurrency(invoice.getSubtotal()));
        variables.put("taxAmount", formatCurrency(invoice.getTaxAmount()));
        variables.put("discountAmount", formatCurrency(invoice.getDiscountAmount()));
        variables.put("totalAmount", formatCurrency(invoice.getTotalAmount()));
        variables.put("paidAmount", formatCurrency(invoice.getPaidAmount()));
        variables.put("balanceDue", formatCurrency(invoice.getBalanceDue()));

        // Items - translate service categories to Arabic
        List<Map<String, Object>> translatedItems = new ArrayList<>();
        if (invoice.getItems() != null) {
            for (InvoiceItemResponse item : invoice.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", item.getId());
                itemMap.put("serviceName", item.getServiceName());
                itemMap.put("serviceCode", item.getServiceCode());
                itemMap.put("description", item.getDescription());
                itemMap.put("category", getServiceCategoryLabel(item.getCategory()));
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unitPrice", formatCurrency(item.getUnitPrice()));
                itemMap.put("discountAmount", formatCurrency(item.getDiscountAmount()));
                itemMap.put("totalPrice", formatCurrency(item.getTotalPrice()));
                itemMap.put("taxable", item.isTaxable());
                itemMap.put("notes", item.getNotes());
                translatedItems.add(itemMap);
            }
        }
        variables.put("items", translatedItems);
        variables.put("hasItems", !translatedItems.isEmpty());

        // Payments - translate payment methods to Arabic
        List<Map<String, Object>> translatedPayments = new ArrayList<>();
        if (invoice.getPayments() != null) {
            for (PaymentResponse payment : invoice.getPayments()) {
                Map<String, Object> paymentMap = new HashMap<>();
                paymentMap.put("id", payment.getId());
                paymentMap.put("amount", payment.getAmount());
                // Payment date is already formatted as string from DTO
                paymentMap.put("paymentDate", payment.getPaymentDate());
                paymentMap.put("paymentMethod", getPaymentMethodLabel(payment.getPaymentMethod()));
                paymentMap.put("referenceNumber", payment.getReferenceNumber());
                paymentMap.put("notes", payment.getNotes());
                translatedPayments.add(paymentMap);
            }
        }
        variables.put("payments", translatedPayments);
        variables.put("hasPayments", !translatedPayments.isEmpty());

        // Notes and terms
        variables.put("notes", invoice.getNotes() != null ? invoice.getNotes() : "");
        variables.put("terms", invoice.getTerms() != null ? invoice.getTerms() : getDefaultTerms());

        // Overdue info
        variables.put("isOverdue", invoice.isOverdue());
        variables.put("daysOverdue", invoice.getDaysOverdue());

        // Logo
        variables.put("logo", getBase64Logo());

        return variables;
    }

    /**
     * Generate PDF from HTML content
     */
    private byte[] generatePdfFromHtml(String htmlContent) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Parse HTML with JSoup and convert to W3C Document
            Document document = Jsoup.parse(htmlContent);
            document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

            // Convert to W3C DOM
            org.w3c.dom.Document w3cDocument = new W3CDom().fromJsoup(document);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withW3cDocument(w3cDocument, null);
            builder.toStream(outputStream);

            // Add fonts with proper Arabic support
            addFonts(builder);

            // Enable RTL support
            builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
            builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
            builder.defaultTextDirection(BaseRendererBuilder.TextDirection.RTL);

            // Run PDF generation
            builder.run();

            logger.info("تم إنشاء ملف PDF بنجاح");
            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("خطأ في توليد PDF: ", e);
            throw new IOException("فشل في توليد PDF", e);
        }
    }

    /**
     * Add fonts for Arabic support
     */
    private void addFonts(PdfRendererBuilder builder) {
        try {
            // Primary Arabic font - Amiri
            InputStream amiriFont = loadFont("Amiri-Regular.ttf");
            if (amiriFont != null) {
                builder.useFont(() -> amiriFont, "Amiri", 400,
                        BaseRendererBuilder.FontStyle.NORMAL, true);
            }

            // Fallback font - Noto Sans Arabic
            InputStream notoFont = loadFont("NotoSansArabic-Regular.ttf");
            if (notoFont != null) {
                builder.useFont(() -> notoFont, "Noto Sans Arabic", 400,
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
                    logger.info("تم تحميل الخط: {}", path);
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
     * Preprocess HTML to ensure OpenHTMLtoPDF compatibility
     */
    private String preprocessHtml(String html) {
        // Parse HTML with JSoup to clean it
        Document doc = Jsoup.parse(html);

        // Ensure all images have absolute paths or are base64
        doc.select("img").forEach(img -> {
            String src = img.attr("src");
            if (!src.startsWith("data:") && !src.startsWith("http")) {
                // Convert relative paths to base64
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

        // Remove unsupported CSS properties for OpenHTMLtoPDF
        style = style.replaceAll("display\\s*:\\s*flex[^;]*;?", "");
        style = style.replaceAll("display\\s*:\\s*grid[^;]*;?", "");
        style = style.replaceAll("gap\\s*:[^;]*;?", "");
        style = style.replaceAll("backdrop-filter\\s*:[^;]*;?", "");
        style = style.replaceAll("filter\\s*:[^;]*;?", "");
        style = style.replaceAll("transform\\s*:[^;]*;?", "");
        style = style.replaceAll("transition\\s*:[^;]*;?", "");
        style = style.replaceAll("animation[^;]*;?", "");
        style = style.replaceAll("opacity\\s*:[^;]*;?", "");

        // Convert rgba colors to hex (simplified conversion)
        style = convertRgbaToHex(style);

        return style.trim();
    }

    /**
     * Convert RGBA colors to hex colors
     */
    private String convertRgbaToHex(String style) {
        Pattern rgbaPattern = Pattern.compile("rgba?\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*(?:,\\s*([\\d.]+))?\\s*\\)");
        Matcher matcher = rgbaPattern.matcher(style);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            int r = Integer.parseInt(matcher.group(1));
            int g = Integer.parseInt(matcher.group(2));
            int b = Integer.parseInt(matcher.group(3));

            String hexColor = String.format("#%02x%02x%02x", r, g, b);
            matcher.appendReplacement(result, hexColor);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Convert image to base64
     */
    private String convertToBase64Image(String imagePath) {
        try {
            ClassPathResource resource = new ClassPathResource(imagePath);
            if (resource.exists()) {
                byte[] imageBytes = IOUtils.toByteArray(resource.getInputStream());
                String base64 = Base64.getEncoder().encodeToString(imageBytes);

                // Determine image type
                String extension = imagePath.substring(imagePath.lastIndexOf(".") + 1).toLowerCase();
                String mimeType = getMimeType(extension);

                return "data:" + mimeType + ";base64," + base64;
            }
        } catch (Exception e) {
            logger.warn("Failed to convert image to base64: {}", imagePath);
        }
        return "";
    }

    /**
     * Get MIME type for image
     */
    private String getMimeType(String extension) {
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            default -> "image/png";
        };
    }

    /**
     * Get company logo as Base64
     */
    private String getBase64Logo() {
        try {
            InputStream logoStream = new ClassPathResource("images/amancare-logo.png").getInputStream();
            byte[] logoBytes = IOUtils.toByteArray(logoStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(logoBytes);
        } catch (Exception e) {
            logger.warn("Failed to load logo image");
            return "";
        }
    }

    /**
     * Format date for display
     */
    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return date.format(arabicDateFormatter);
    }

    /**
     * Format currency for display
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%.2f", amount);
    }

    /**
     * Get status label in Arabic
     */
    private String getStatusLabel(com.nakqeeb.amancare.entity.InvoiceStatus status) {
        return switch (status) {
            case DRAFT -> "مسودة";
            case PENDING -> "معلقة";
            case SENT -> "مرسلة";
            case VIEWED -> "تمت المشاهدة";
            case PAID -> "مدفوعة";
            case PARTIALLY_PAID -> "مدفوعة جزئياً";
            case OVERDUE -> "متأخرة";
            case CANCELLED -> "ملغية";
            case REFUNDED -> "مستردة";
            default -> status.toString();
        };
    }

    /**
     * Get payment status label in Arabic
     */
    private String getPaymentStatusLabel(com.nakqeeb.amancare.entity.PaymentStatus status) {
        return switch (status) {
            case PENDING -> "معلق";
            case COMPLETED -> "مكتمل";
            case FAILED -> "فشل";
            case CANCELLED -> "ملغي";
            case REFUNDED -> "مسترد";
            default -> status.toString();
        };
    }

    /**
     * Get status color for display
     */
    private String getStatusColor(com.nakqeeb.amancare.entity.InvoiceStatus status) {
        return switch (status) {
            case DRAFT -> "#9e9e9e";
            case PENDING -> "#ff9800";
            case SENT, VIEWED -> "#2196f3";
            case PAID -> "#4caf50";
            case PARTIALLY_PAID -> "#8bc34a";
            case OVERDUE -> "#f44336";
            case CANCELLED, REFUNDED -> "#757575";
            default -> "#333";
        };
    }

    /**
     * Get default payment terms
     */
    private String getDefaultTerms() {
        return "الرجاء الدفع خلال فترة الاستحقاق المحددة. في حالة التأخر في الدفع، قد يتم تطبيق رسوم إضافية.";
    }

    /**
     * Get payment method label in Arabic
     */
    public String getPaymentMethodLabel(com.nakqeeb.amancare.entity.PaymentMethod method) {
        return switch (method) {
            case CASH -> "نقدي";
            case CREDIT_CARD -> "بطاقة ائتمان";
            case DEBIT_CARD -> "بطاقة مدين";
            case BANK_TRANSFER -> "حوالة بنكية";
            case CHECK -> "شيك";
            case INSURANCE -> "تأمين";
            case INSTALLMENT -> "أقساط";
            case ONLINE -> "دفع إلكتروني";
            default -> method.toString();
        };
    }

    /**
     * Get service category label in Arabic
     */
    public String getServiceCategoryLabel(com.nakqeeb.amancare.entity.ServiceCategory category) {
        return switch (category) {
            case CONSULTATION -> "استشارة";
            case PROCEDURE -> "إجراء طبي";
            case MEDICATION -> "أدوية";
            case LAB_TEST -> "فحوصات مخبرية";
            case RADIOLOGY -> "أشعة";
            case SURGERY -> "جراحة";
            case THERAPY -> "علاج طبيعي";
            case VACCINATION -> "تطعيم";
            case EQUIPMENT -> "معدات طبية";
            case OTHER -> "أخرى";
            default -> category.toString();
        };
    }
}