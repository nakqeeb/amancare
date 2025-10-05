// =============================================================================
// Invoice Controller - وحدة التحكم بالفواتير
// src/main/java/com/nakqeeb/amancare/controller/InvoiceController.java
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.annotation.SystemAdminContext;
import com.nakqeeb.amancare.dto.request.CreateInvoiceRequest;
import com.nakqeeb.amancare.dto.request.UpdateInvoiceRequest;
import com.nakqeeb.amancare.dto.request.CreatePaymentRequest;
import com.nakqeeb.amancare.dto.request.InvoiceSearchCriteria;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.entity.InvoiceStatus;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.InvoiceService;
import com.nakqeeb.amancare.service.ClinicContextService;
import com.nakqeeb.amancare.service.pdf.PdfInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * وحدة التحكم بالفواتير والمدفوعات
 * REST Controller for invoice and payment management
 */
@RestController
@RequestMapping("/invoices")
@Tag(name = "💰 إدارة الفواتير", description = "APIs الخاصة بإدارة الفواتير والمدفوعات")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InvoiceController {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private ClinicContextService clinicContextService;

    @Autowired
    private PdfInvoiceService pdfInvoiceService;

    // ===================================================================
    // INVOICE OPERATIONS
    // ===================================================================

    /**
     * Create a new invoice
     */
    @PostMapping
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    @Operation(
            summary = "➕ إنشاء فاتورة جديدة",
            description = """
            إنشاء فاتورة جديدة للمريض مع إضافة الخدمات والرسوم
            - SYSTEM_ADMIN: يحتاج سياق العيادة
            - باقي الأدوار: ينشئون في عيادتهم مباشرة
            
            Headers المطلوبة لـ SYSTEM_ADMIN:
            - X-Acting-Clinic-Id: معرف العيادة
            - X-Acting-Reason: سبب الإنشاء
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "مثال فاتورة استشارة",
                                    value = """
                                    {
                                      "patientId": 1,
                                      "appointmentId": 5,
                                      "dueDate": "2025-02-01",
                                      "items": [
                                        {
                                          "serviceName": "استشارة طبية",
                                          "serviceCode": "CON001",
                                          "description": "استشارة أولية",
                                          "category": "CONSULTATION",
                                          "quantity": 1,
                                          "unitPrice": 150.00,
                                          "taxable": true
                                        },
                                        {
                                          "serviceName": "فحص ضغط الدم",
                                          "category": "PROCEDURE",
                                          "quantity": 1,
                                          "unitPrice": 25.00
                                        }
                                      ],
                                      "taxPercentage": 15,
                                      "discountAmount": 10,
                                      "notes": "استشارة أولية - مريض جديد",
                                      "terms": "الدفع خلال 30 يوم"
                                    }
                                    """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "تم إنشاء الفاتورة بنجاح"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "بيانات غير صحيحة"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "المريض أو الموعد غير موجود"
            )
    })
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // For SYSTEM_ADMIN, validate clinic context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                Long clinicId = clinicContextService.getEffectiveClinicId(currentUser);
                logger.info("SYSTEM_ADMIN creating invoice in clinic: {}", clinicId);
            }

            InvoiceResponse invoice = invoiceService.createInvoice(request, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(true, "تم إنشاء الفاتورة بنجاح", invoice)
            );
        } catch (Exception e) {
            logger.error("Error creating invoice: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل إنشاء الفاتورة: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Get all invoices with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE')")
    @Operation(
            summary = "📋 قائمة الفواتير",
            description = """
            الحصول على قائمة الفواتير مع إمكانية التصفية والبحث
            - SYSTEM_ADMIN: يمكنه عرض فواتير أي عيادة
            - باقي الأدوار: يعرضون فواتير عيادتهم فقط
            """
    )
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getAllInvoices(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف المريض")
            @RequestParam(required = false) Long patientId,
            @Parameter(description = "معرف العيادة (SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "حالة الفاتورة")
            @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "من تاريخ")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "إلى تاريخ")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "الحد الأدنى للمبلغ")
            @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "الحد الأقصى للمبلغ")
            @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "رقم الصفحة", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ترتيب حسب", example = "invoiceDate")
            @RequestParam(defaultValue = "invoiceDate") String sortBy,
            @Parameter(description = "اتجاه الترتيب", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            InvoiceSearchCriteria criteria = new InvoiceSearchCriteria();
            criteria.setPatientId(patientId);
            criteria.setClinicId(clinicId);
            criteria.setStatus(status);
            criteria.setFromDate(fromDate);
            criteria.setToDate(toDate);
            criteria.setMinAmount(minAmount);
            criteria.setMaxAmount(maxAmount);
            criteria.setSortBy(sortBy);
            criteria.setSortDirection(sortDirection);

            Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<InvoiceResponse> invoices = invoiceService.getAllInvoices(pageable, criteria, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب الفواتير بنجاح", invoices)
            );
        } catch (Exception e) {
            logger.error("Error fetching invoices: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل جلب الفواتير: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Get invoice by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE')")
    @Operation(
            summary = "🔍 تفاصيل الفاتورة",
            description = "الحصول على تفاصيل فاتورة محددة مع جميع البنود والمدفوعات"
    )
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Fetching invoice {} by user: {}", id, currentUser.getId());

            InvoiceResponse invoice = invoiceService.getInvoiceById(id, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب تفاصيل الفاتورة بنجاح", invoice)
            );
        } catch (Exception e) {
            logger.error("Error fetching invoice {}: ", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(false, "الفاتورة غير موجودة: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Update invoice
     */
    @PutMapping("/{id}")
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'RECEPTIONIST')")
    @Operation(
            summary = "✏️ تحديث الفاتورة",
            description = """
            تحديث بيانات الفاتورة (لا يمكن تعديل الفواتير المدفوعة أو الملغية)
            
            Headers المطلوبة لـ SYSTEM_ADMIN:
            - X-Acting-Clinic-Id: معرف العيادة
            - X-Acting-Reason: سبب التحديث
            """
    )
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvoiceRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // For SYSTEM_ADMIN, validate clinic context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                Long clinicId = clinicContextService.getEffectiveClinicId(currentUser);
                logger.info("SYSTEM_ADMIN updating invoice {} in clinic: {}", id, clinicId);
            }

            InvoiceResponse invoice = invoiceService.updateInvoice(id, request, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تحديث الفاتورة بنجاح", invoice)
            );
        } catch (Exception e) {
            logger.error("Error updating invoice {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل تحديث الفاتورة: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Cancel invoice
     */
    @PutMapping("/{id}/cancel")
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN')")
    @Operation(
            summary = "❌ إلغاء الفاتورة",
            description = """
            إلغاء فاتورة (لا يمكن إلغاء الفواتير المدفوعة)
            
            Headers المطلوبة لـ SYSTEM_ADMIN:
            - X-Acting-Clinic-Id: معرف العيادة
            - X-Acting-Reason: سبب الإلغاء
            """
    )
    public ResponseEntity<ApiResponse<InvoiceResponse>> cancelInvoice(
            @PathVariable Long id,
            @Parameter(description = "سبب الإلغاء", required = true)
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // For SYSTEM_ADMIN, validate clinic context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                Long clinicId = clinicContextService.getEffectiveClinicId(currentUser);
                logger.info("SYSTEM_ADMIN cancelling invoice {} in clinic: {} - Reason: {}", id, clinicId, reason);
            }

            InvoiceResponse invoice = invoiceService.cancelInvoice(id, reason, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم إلغاء الفاتورة بنجاح", invoice)
            );
        } catch (Exception e) {
            logger.error("Error cancelling invoice {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل إلغاء الفاتورة: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Send invoice to patient
     */
    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'RECEPTIONIST')")
    @Operation(
            summary = "📧 إرسال الفاتورة",
            description = "إرسال الفاتورة للمريض عبر البريد الإلكتروني أو الرسائل النصية"
    )
    public ResponseEntity<ApiResponse<InvoiceResponse>> sendInvoice(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Sending invoice {} by user: {}", id, currentUser.getId());

            InvoiceResponse invoice = invoiceService.sendInvoice(id, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم إرسال الفاتورة بنجاح", invoice)
            );
        } catch (Exception e) {
            logger.error("Error sending invoice {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل إرسال الفاتورة: " + e.getMessage(), null)
            );
        }
    }

    // ===================================================================
    // PAYMENT OPERATIONS
    // ===================================================================

    /**
     * Add payment to invoice
     */
    @PostMapping("/{id}/payments")
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'RECEPTIONIST')")
    @Operation(
            summary = "💵 إضافة دفعة",
            description = """
            إضافة دفعة للفاتورة مع تحديث حالة الدفع
            
            Headers المطلوبة لـ SYSTEM_ADMIN:
            - X-Acting-Clinic-Id: معرف العيادة
            - X-Acting-Reason: سبب الدفع
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "مثال دفعة نقدية",
                                    value = """
                                    {
                                      "invoiceId": 1,
                                      "amount": 175.00,
                                      "paymentMethod": "CASH",
                                      "referenceNumber": "CASH-2025-001",
                                      "notes": "دفعة كاملة نقداً"
                                    }
                                    """
                            )
                    )
            )
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> addPayment(
            @PathVariable Long id,
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // Ensure invoice ID matches
            request.setInvoiceId(id);

            // For SYSTEM_ADMIN, validate clinic context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                Long clinicId = clinicContextService.getEffectiveClinicId(currentUser);
                logger.info("SYSTEM_ADMIN adding payment to invoice {} in clinic: {}", id, clinicId);
            }

            PaymentResponse payment = invoiceService.addPayment(request, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(true, "تم إضافة الدفعة بنجاح", payment)
            );
        } catch (Exception e) {
            logger.error("Error adding payment to invoice {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل إضافة الدفعة: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Get payments for invoice
     */
    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'RECEPTIONIST')")
    @Operation(
            summary = "💳 مدفوعات الفاتورة",
            description = "الحصول على جميع المدفوعات المرتبطة بالفاتورة"
    )
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getInvoicePayments(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Fetching payments for invoice {} by user: {}", id, currentUser.getId());

            List<PaymentResponse> payments = invoiceService.getInvoicePayments(id, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب المدفوعات بنجاح", payments)
            );
        } catch (Exception e) {
            logger.error("Error fetching payments for invoice {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل جلب المدفوعات: " + e.getMessage(), null)
            );
        }
    }

    // ===================================================================
    // STATISTICS AND REPORTS
    // ===================================================================

    /**
     * Get invoice statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN')")
    @Operation(
            summary = "📊 إحصائيات الفواتير",
            description = "الحصول على إحصائيات شاملة للفواتير والمدفوعات"
    )
    public ResponseEntity<ApiResponse<InvoiceStatisticsResponse>> getInvoiceStatistics(
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // Determine clinic ID
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                effectiveClinicId = clinicId != null ? clinicId : currentUser.getClinicId();
            } else {
                effectiveClinicId = currentUser.getClinicId();
            }

            logger.info("Fetching invoice statistics for clinic {} by user: {}", effectiveClinicId, currentUser.getId());

            InvoiceStatisticsResponse statistics = invoiceService.getInvoiceStatistics(effectiveClinicId, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب الإحصائيات بنجاح", statistics)
            );
        } catch (Exception e) {
            logger.error("Error fetching invoice statistics: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل جلب الإحصائيات: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Get overdue invoices
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'RECEPTIONIST')")
    @Operation(
            summary = "⚠️ الفواتير المتأخرة",
            description = "الحصول على قائمة الفواتير التي تجاوزت تاريخ الاستحقاق"
    )
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getOverdueInvoices(
            @Parameter(description = "معرف العيادة")
            @RequestParam(required = false) Long clinicId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // Determine clinic ID
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                effectiveClinicId = clinicId != null ? clinicId : currentUser.getClinicId();
            } else {
                effectiveClinicId = currentUser.getClinicId();
            }

            logger.info("Fetching overdue invoices for clinic {} by user: {}", effectiveClinicId, currentUser.getId());

            List<InvoiceResponse> overdueInvoices = invoiceService.getOverdueInvoices(effectiveClinicId, currentUser);

            String message = String.format("تم جلب %d فاتورة متأخرة", overdueInvoices.size());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, message, overdueInvoices)
            );
        } catch (Exception e) {
            logger.error("Error fetching overdue invoices: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل جلب الفواتير المتأخرة: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Get invoices by patient
     * UPDATED to use the new dedicated service method
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE')")
    @Operation(
            summary = "👤 فواتير المريض",
            description = "الحصول على جميع فواتير مريض محدد"
    )
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getPatientInvoices(
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Fetching invoices for patient {} by user: {}", patientId, currentUser.getId());

            // Use the new dedicated method instead of getAllInvoices
            Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "invoiceDate"));
            Page<InvoiceResponse> invoices = invoiceService.getPatientInvoices(patientId, pageable, currentUser);

            String message = String.format("تم جلب %d فاتورة للمريض", invoices.getTotalElements());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, message, invoices.getContent())
            );
        } catch (Exception e) {
            logger.error("Error fetching patient invoices: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل جلب فواتير المريض: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Export invoice as PDF
     * تصدير الفاتورة كملف PDF
     */
    @GetMapping("/{id}/export/pdf")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'RECEPTIONIST')")
    @Operation(
            summary = "📄 تصدير الفاتورة PDF",
            description = """
                تصدير الفاتورة الكاملة في ملف PDF احترافي
                - معلومات الفاتورة والمريض
                - جدول الخدمات والأسعار
                - تفاصيل الدفعات
                - الشروط والأحكام
                """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "تم تصدير الفاتورة بنجاح",
                            content = @Content(mediaType = "application/pdf")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "الفاتورة غير موجودة"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "خطأ في توليد ملف PDF"
                    )
            }
    )
    public ResponseEntity<byte[]> exportInvoicePdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        logger.info("تصدير فاتورة PDF رقم {} بواسطة المستخدم: {}", id, currentUser.getUsername());

        try {
            // Fetch invoice details
            InvoiceResponse invoice = invoiceService.getInvoiceById(id, currentUser);

            // Generate PDF
            byte[] pdfContent = pdfInvoiceService.generateInvoicePdf(invoice);

            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = String.format("invoice_%s_%s.pdf",
                    invoice.getInvoiceNumber(),
                    LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            logger.info("تم إنشاء ملف PDF للفاتورة {} بنجاح", invoice.getInvoiceNumber());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);

        } catch (ResourceNotFoundException e) {
            logger.error("الفاتورة غير موجودة: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("خطأ في تصدير الفاتورة PDF: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export invoice receipt as PDF (simplified version)
     * تصدير إيصال الدفع كملف PDF
     */
    @GetMapping("/{id}/export/receipt")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'RECEPTIONIST')")
    @Operation(
            summary = "🧾 تصدير إيصال الدفع PDF",
            description = """
                تصدير إيصال دفع مختصر للفاتورة
                - معلومات المريض والفاتورة
                - المبلغ المدفوع والمتبقي
                - تفاصيل الدفعة الأخيرة
                - تصميم مبسط للطباعة السريعة
                """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "تم تصدير الإيصال بنجاح",
                            content = @Content(mediaType = "application/pdf")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "الفاتورة غير موجودة"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "خطأ في توليد ملف PDF"
                    )
            }
    )
    public ResponseEntity<byte[]> exportInvoiceReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        logger.info("تصدير إيصال PDF للفاتورة {} بواسطة المستخدم: {}", id, currentUser.getUsername());

        try {
            // Fetch invoice details
            InvoiceResponse invoice = invoiceService.getInvoiceById(id, currentUser);

            // Validate that invoice has payments
            if (invoice.getPayments() == null || invoice.getPayments().isEmpty()) {
                logger.warn("الفاتورة {} لا تحتوي على دفعات", id);
                return ResponseEntity.badRequest().build();
            }

            // Generate receipt PDF
            byte[] pdfContent = pdfInvoiceService.generateInvoiceReceiptPdf(invoice);

            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = String.format("receipt_%s_%s.pdf",
                    invoice.getInvoiceNumber(),
                    LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            logger.info("تم إنشاء إيصال PDF للفاتورة {} بنجاح", invoice.getInvoiceNumber());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);

        } catch (ResourceNotFoundException e) {
            logger.error("الفاتورة غير موجودة: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("خطأ في تصدير إيصال PDF: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Preview invoice PDF in browser (inline display)
     * معاينة الفاتورة PDF في المتصفح
     */
    @GetMapping("/{id}/preview/pdf")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'RECEPTIONIST')")
    @Operation(
            summary = "👁️ معاينة الفاتورة PDF",
            description = "عرض الفاتورة في المتصفح بدون تحميل"
    )
    public ResponseEntity<byte[]> previewInvoicePdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        logger.info("معاينة فاتورة PDF رقم {} بواسطة المستخدم: {}", id, currentUser.getUsername());

        try {
            // Fetch invoice details
            InvoiceResponse invoice = invoiceService.getInvoiceById(id, currentUser);

            // Generate PDF
            byte[] pdfContent = pdfInvoiceService.generateInvoicePdf(invoice);

            // Prepare response headers for inline display
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = String.format("invoice_%s.pdf", invoice.getInvoiceNumber());
            headers.add("Content-Disposition", "inline; filename=\"" + filename + "\"");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);

        } catch (ResourceNotFoundException e) {
            logger.error("الفاتورة غير موجودة: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("خطأ في معاينة الفاتورة PDF: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}