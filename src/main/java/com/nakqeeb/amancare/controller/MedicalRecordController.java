// =============================================================================
// Medical Record Controller - وحدة التحكم بالسجلات الطبية
// src/main/java/com/nakqeeb/amancare/controller/MedicalRecordController.java
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.annotation.SystemAdminContext;
import com.nakqeeb.amancare.dto.request.healthrecords.*;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.dto.response.healthrecords.*;
import com.nakqeeb.amancare.entity.healthrecords.RecordStatus;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.entity.healthrecords.VisitType;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.ClinicContextService;
import com.nakqeeb.amancare.service.MedicalRecordService;
import com.nakqeeb.amancare.service.pdf.PdfMedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * وحدة التحكم بالسجلات الطبية
 * REST Controller for Medical Records Management
 */
@RestController
@RequestMapping("/medical-records")
@Tag(name = "🩺 إدارة السجلات الطبية", description = "APIs الخاصة بإدارة السجلات والتاريخ الطبي للمرضى")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final ClinicContextService clinicContextService;
    private final PdfMedicalRecordService pdfMedicalRecordService;

    // =============================================================================
    // CREATE OPERATIONS
    // =============================================================================

    /**
     * إنشاء سجل طبي جديد
     */
    @PostMapping
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR')")
    @Operation(
            summary = "➕ إنشاء سجل طبي جديد",
            description = """
            إضافة سجل طبي جديد للمريض مع التفاصيل الكاملة للزيارة:
            - العلامات الحيوية والفحص السريري
            - التشخيص والعلاج والوصفات الطبية
            - الفحوصات المخبرية والإشعاعية
            - الإجراءات الطبية والتحويلات
            
            Headers المطلوبة لـ SYSTEM_ADMIN:
            - X-Acting-Clinic-Id: معرف العيادة
            - X-Acting-Reason: سبب العملية
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "مثال سجل طبي كامل",
                                    value = """
                                    {
                                      "patientId": 1,
                                      "appointmentId": 5,
                                      "doctorId": 2,
                                      "visitDate": "2025-01-15",
                                      "visitType": "CONSULTATION",
                                      "vitalSigns": {
                                        "temperature": 37.2,
                                        "bloodPressureSystolic": 120,
                                        "bloodPressureDiastolic": 80,
                                        "heartRate": 72,
                                        "weight": 70.5,
                                        "height": 175.0
                                      },
                                      "chiefComplaint": "ألم في الصدر وضيق في التنفس",
                                      "presentIllness": "يشكو المريض من ألم في الصدر منذ يومين مع ضيق تنفس عند الجهد",
                                      "physicalExamination": "المريض واعي ومتعاون، الفحص السريري طبيعي",
                                      "diagnosis": [
                                        {
                                          "description": "التهاب الجهاز التنفسي العلوي",
                                          "type": "PRIMARY",
                                          "isPrimary": true
                                        }
                                      ],
                                      "treatmentPlan": "راحة تامة مع تناول السوائل والأدوية الموصوفة",
                                      "prescriptions": [
                                        {
                                          "medicationName": "باراسيتامول",
                                          "dosage": "500mg",
                                          "frequency": "كل 6 ساعات",
                                          "duration": "5 أيام",
                                          "route": "ORAL"
                                        }
                                      ],
                                      "followUpDate": "2025-01-22",
                                      "status": "COMPLETED"
                                    }
                                    """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "تم إنشاء السجل الطبي بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "بيانات غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "المريض أو الطبيب غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "يوجد سجل طبي مرتبط بالموعد بالفعل"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> createMedicalRecord(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateMedicalRecordRequest request) {
        try {
            // Log if SYSTEM_ADMIN is acting with context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                ClinicContextService.ClinicContextInfo contextInfo =
                        clinicContextService.getCurrentContext(currentUser);
                log.info("مدير النظام ينشئ سجلاً طبياً في سياق العيادة. معرف العيادة المؤقت: {}, السبب: {}",
                        contextInfo.getActingAsClinicId(), contextInfo.getReason());
            }

            MedicalRecordResponse medicalRecord = medicalRecordService.createMedicalRecord(request, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "تم إنشاء السجل الطبي بنجاح", medicalRecord));

        } catch (Exception e) {
            log.error("خطأ في إنشاء السجل الطبي: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في إنشاء السجل الطبي: " + e.getMessage(), null));
        }
    }

    // =============================================================================
    // READ OPERATIONS
    // =============================================================================

    /**
     * الحصول على جميع السجلات الطبية
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "📋 قائمة السجلات الطبية",
            description = """
            الحصول على قائمة السجلات الطبية مع إمكانية الترقيم والترتيب:
            - SYSTEM_ADMIN: يمكنه الوصول لسجلات جميع العيادات
            - باقي الأدوار: سجلات عيادتهم فقط
            - يتم إخفاء السجلات السرية حسب الصلاحيات
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على قائمة السجلات بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<Page<MedicalRecordSummaryResponse>>> getAllMedicalRecords(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "رقم الصفحة", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ترتيب حسب", example = "visitDate")
            @RequestParam(defaultValue = "visitDate") String sortBy,
            @Parameter(description = "اتجاه الترتيب", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<MedicalRecordSummaryResponse> medicalRecords =
                    medicalRecordService.getAllMedicalRecords(pageable, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب قائمة السجلات الطبية بنجاح", medicalRecords)
            );
        } catch (Exception e) {
            log.error("خطأ في جلب السجلات الطبية: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في جلب السجلات الطبية: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على سجل طبي بالمعرف
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "🔍 تفاصيل السجل الطبي",
            description = """
            الحصول على تفاصيل سجل طبي محدد مع جميع المعلومات:
            - بيانات المريض والطبيب
            - العلامات الحيوية والفحوصات
            - التشخيص والعلاج
            - الوصفات والتحاليل والأشعة
            - التحويلات والمتابعة
            
            ملاحظة: السجلات السرية تتطلب صلاحيات خاصة
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على تفاصيل السجل بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "السجل الطبي غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "غير مصرح بالوصول لهذا السجل"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول")
    })
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getMedicalRecordById(
            @Parameter(description = "معرف السجل الطبي", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                log.info("مدير النظام يقرأ السجل الطبي {} من العيادة: {}",
                        id, clinicId != null ? clinicId : "جميع العيادات");
            }

            MedicalRecordResponse medicalRecord = medicalRecordService.getMedicalRecordById(id, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على تفاصيل السجل الطبي بنجاح", medicalRecord)
            );
        } catch (Exception e) {
            log.error("خطأ في جلب السجل الطبي {}: ", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في جلب السجل الطبي: " + e.getMessage(), null));
        }
    }

    /**
     * البحث في السجلات الطبية
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "🔍 البحث المتقدم في السجلات الطبية",
            description = """
            البحث المتقدم في السجلات الطبية باستخدام معايير متنوعة:
            - البحث النصي في الشكوى والتشخيص والملاحظات
            - التصفية حسب المريض أو الطبيب
            - التصفية حسب نوع الزيارة أو الحالة
            - التصفية حسب التاريخ
            - السجلات السرية (حسب الصلاحيات)
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم البحث بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "معايير البحث غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<Page<MedicalRecordSummaryResponse>>> searchMedicalRecords(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "معرف المريض")
            @RequestParam(required = false) Long patientId,
            @Parameter(description = "معرف الطبيب")
            @RequestParam(required = false) Long doctorId,
            @Parameter(description = "نوع الزيارة")
            @RequestParam(required = false) String visitType,
            @Parameter(description = "حالة السجل")
            @RequestParam(required = false) String status,
            @Parameter(description = "تاريخ البداية", example = "2024-01-01")
            @RequestParam(required = false) String visitDateFrom,
            @Parameter(description = "تاريخ النهاية", example = "2024-12-31")
            @RequestParam(required = false) String visitDateTo,
            @Parameter(description = "السجلات السرية فقط")
            @RequestParam(required = false) Boolean isConfidential,
            @Parameter(description = "كلمة البحث")
            @RequestParam(required = false) String searchTerm,
            @Parameter(description = "رقم الصفحة", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ترتيب حسب", example = "visitDate")
            @RequestParam(defaultValue = "visitDate") String sortBy,
            @Parameter(description = "اتجاه الترتيب", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            // For READ operations, SYSTEM_ADMIN can specify clinic
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                log.info("مدير النظام يبحث في السجلات الطبية من العيادة: {}",
                        clinicId != null ? clinicId : "جميع العيادات");
            }

            MedicalRecordSearchCriteria criteria = MedicalRecordSearchCriteria.builder()
                    .clinicId(clinicId)
                    .patientId(patientId)
                    .doctorId(doctorId)
                    .visitType(visitType != null ? VisitType.valueOf(visitType) : null)
                    .status(status != null ? RecordStatus.valueOf(status) : null)
                    .visitDateFrom(visitDateFrom != null ? LocalDate.parse(visitDateFrom) : null)
                    .visitDateTo(visitDateTo != null ? LocalDate.parse(visitDateTo) : null)
                    .isConfidential(isConfidential)
                    .searchTerm(searchTerm)
                    .build();

            Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<MedicalRecordSummaryResponse> searchResults =
                    medicalRecordService.searchMedicalRecords(criteria, pageable, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم البحث في السجلات الطبية بنجاح", searchResults)
            );
        } catch (Exception e) {
            log.error("خطأ في البحث في السجلات الطبية: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في البحث: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على التاريخ الطبي للمريض
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "📊 التاريخ الطبي للمريض",
            description = """
            الحصول على التاريخ الطبي الكامل لمريض معين مرتب حسب التاريخ:
            - جميع الزيارات والفحوصات
            - التشخيصات والعلاجات السابقة
            - الوصفات الطبية والتحاليل
            - تطور الحالة عبر الزمن
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على التاريخ الطبي بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "المريض غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<Page<MedicalRecordSummaryResponse>>> getPatientMedicalHistory(
            @Parameter(description = "معرف المريض", example = "1")
            @PathVariable Long patientId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "رقم الصفحة", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<MedicalRecordSummaryResponse> medicalHistory =
                    medicalRecordService.getPatientMedicalHistory(patientId, pageable, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على التاريخ الطبي للمريض بنجاح", medicalHistory)
            );
        } catch (Exception e) {
            log.error("خطأ في جلب التاريخ الطبي للمريض {}: ", patientId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في جلب التاريخ الطبي: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على السجلات الطبية للطبيب
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE')")
    @Operation(
            summary = "👨‍⚕️ سجلات الطبيب الطبية",
            description = """
            الحصول على السجلات الطبية التي أنشأها طبيب معين:
            - جميع المرضى الذين عالجهم الطبيب
            - إحصائيات أداء الطبيب
            - أنواع الحالات والتشخيصات
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على سجلات الطبيب بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الطبيب غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<Page<MedicalRecordSummaryResponse>>> getDoctorMedicalRecords(
            @Parameter(description = "معرف الطبيب", example = "2")
            @PathVariable Long doctorId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "رقم الصفحة", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<MedicalRecordSummaryResponse> doctorRecords =
                    medicalRecordService.getDoctorMedicalRecords(doctorId, pageable, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على سجلات الطبيب بنجاح", doctorRecords)
            );
        } catch (Exception e) {
            log.error("خطأ في جلب سجلات الطبيب {}: ", doctorId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في جلب سجلات الطبيب: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على السجل الطبي المرتبط بالموعد
     */
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "📅 السجل الطبي للموعد",
            description = """
            الحصول على السجل الطبي المرتبط بموعد محدد:
            - تفاصيل الزيارة والفحص
            - التشخيص والعلاج المقدم
            - النتائج والتوصيات
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على السجل الطبي بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "لا يوجد سجل طبي مرتبط بهذا الموعد"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getMedicalRecordByAppointment(
            @Parameter(description = "معرف الموعد", example = "5")
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            MedicalRecordResponse medicalRecord =
                    medicalRecordService.getMedicalRecordByAppointment(appointmentId, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على السجل الطبي للموعد بنجاح", medicalRecord)
            );
        } catch (Exception e) {
            log.error("خطأ في جلب السجل الطبي للموعد {}: ", appointmentId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في جلب السجل الطبي: " + e.getMessage(), null));
        }
    }

    // =============================================================================
    // UPDATE OPERATIONS
    // =============================================================================

    /**
     * تحديث السجل الطبي
     */
    @PutMapping("/{id}")
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR')")
    @Operation(
            summary = "✏️ تحديث السجل الطبي",
            description = """
            تحديث تفاصيل السجل الطبي:
            - لا يمكن تعديل السجلات المقفلة أو الملغية
            - فقط منشئ السجل أو المدير يمكنه التعديل
            - يتم حفظ سجل التعديلات لأغراض المراجعة
            
            Headers المطلوبة لـ SYSTEM_ADMIN:
            - X-Acting-Clinic-Id: معرف العيادة
            - X-Acting-Reason: سبب التحديث
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم تحديث السجل بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "بيانات التحديث غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "السجل الطبي غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "لا يمكن تعديل هذا السجل"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> updateMedicalRecord(
            @Parameter(description = "معرف السجل الطبي", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UpdateMedicalRecordRequest request) {
        try {
            // Log if SYSTEM_ADMIN is acting with context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                ClinicContextService.ClinicContextInfo contextInfo =
                        clinicContextService.getCurrentContext(currentUser);
                log.info("مدير النظام يحدث سجلاً طبياً في سياق العيادة. معرف العيادة المؤقت: {}, السبب: {}",
                        contextInfo.getActingAsClinicId(), contextInfo.getReason());
            }

            MedicalRecordResponse updatedRecord =
                    medicalRecordService.updateMedicalRecord(id, request, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تحديث السجل الطبي بنجاح", updatedRecord)
            );
        } catch (Exception e) {
            log.error("خطأ في تحديث السجل الطبي {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في تحديث السجل الطبي: " + e.getMessage(), null));
        }
    }

    /**
     * تحديث حالة السجل الطبي
     */
    @PutMapping("/{id}/status")
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR')")
    @Operation(
            summary = "🔄 تحديث حالة السجل الطبي",
            description = """
            تغيير حالة السجل الطبي:
            - مسودة → قيد التحرير → مكتمل → مراجع → مقفل
            - فقط المديرون يمكنهم إلغاء قفل السجلات
            - يتم تسجيل جميع تغييرات الحالة
            
            Headers المطلوبة لـ SYSTEM_ADMIN:
            - X-Acting-Clinic-Id: معرف العيادة
            - X-Acting-Reason: سبب تغيير الحالة
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "تغيير حالة السجل",
                                    value = """
                                    {
                                      "status": "COMPLETED",
                                      "notes": "تم اكتمال الفحص وتسجيل جميع النتائج"
                                    }
                                    """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم تحديث حالة السجل بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "الحالة الجديدة غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "السجل الطبي غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "لا يمكن تغيير الحالة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> updateRecordStatus(
            @Parameter(description = "معرف السجل الطبي", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UpdateRecordStatusRequest request) {
        try {
            // Log if SYSTEM_ADMIN is acting with context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                ClinicContextService.ClinicContextInfo contextInfo =
                        clinicContextService.getCurrentContext(currentUser);
                log.info("مدير النظام يحدث حالة سجل طبي في سياق العيادة. معرف العيادة المؤقت: {}, السبب: {}",
                        contextInfo.getActingAsClinicId(), contextInfo.getReason());
            }

            MedicalRecordResponse updatedRecord =
                    medicalRecordService.updateRecordStatus(id, request, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تحديث حالة السجل الطبي بنجاح", updatedRecord)
            );
        } catch (Exception e) {
            log.error("خطأ في تحديث حالة السجل الطبي {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في تحديث حالة السجل: " + e.getMessage(), null));
        }
    }

    // =============================================================================
    // DELETE OPERATIONS
    // =============================================================================

    /**
     * حذف السجل الطبي
     */
    @DeleteMapping("/{id}")
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN')")
    @Operation(
            summary = "🗑️ حذف السجل الطبي",
            description = """
            حذف سجل طبي (حذف منطقي):
            - يتم تعديل حالة السجل إلى "ملغي" بدلاً من الحذف النهائي
            - فقط المديرون يمكنهم حذف السجلات
            - لا يمكن حذف السجلات المقفلة
            - يتم الاحتفاظ بسجل العملية لأغراض المراجعة
            
            Headers المطلوبة لـ SYSTEM_ADMIN:
            - X-Acting-Clinic-Id: معرف العيادة
            - X-Acting-Reason: سبب الحذف
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم حذف السجل بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "السجل الطبي غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "لا يمكن حذف هذا السجل"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<Void>> deleteMedicalRecord(
            @Parameter(description = "معرف السجل الطبي", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // Log if SYSTEM_ADMIN is acting with context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                ClinicContextService.ClinicContextInfo contextInfo =
                        clinicContextService.getCurrentContext(currentUser);
                log.info("مدير النظام يحذف سجلاً طبياً في سياق العيادة. معرف العيادة المؤقت: {}, السبب: {}",
                        contextInfo.getActingAsClinicId(), contextInfo.getReason());
            }

            medicalRecordService.deleteMedicalRecord(id, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم حذف السجل الطبي بنجاح", null)
            );
        } catch (Exception e) {
            log.error("خطأ في حذف السجل الطبي {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في حذف السجل الطبي: " + e.getMessage(), null));
        }
    }

    /**
     * الحذف النهائي للسجل الطبي (مدير النظام فقط)
     */
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "💀 الحذف النهائي للسجل الطبي",
            description = """
            الحذف النهائي للسجل الطبي من قاعدة البيانات:
            ⚠️ تحذير: هذه العملية لا يمكن التراجع عنها!
            - فقط مدير النظام يمكنه الحذف النهائي
            - يتم حذف جميع البيانات المرتبطة (التشخيص، الوصفات، إلخ)
            - يُنصح باستخدام الحذف المنطقي بدلاً من ذلك
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحذف النهائي بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "السجل الطبي غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - مدير النظام فقط")
    })
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteMedicalRecord(
            @Parameter(description = "معرف السجل الطبي", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            medicalRecordService.permanentlyDeleteMedicalRecord(id, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحذف النهائي للسجل الطبي بنجاح", null)
            );
        } catch (Exception e) {
            log.error("خطأ في الحذف النهائي للسجل الطبي {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في الحذف النهائي: " + e.getMessage(), null));
        }
    }

    // =============================================================================
    // STATISTICS AND REPORTS
    // =============================================================================

    /**
     * إحصائيات السجلات الطبية
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR')")
    @Operation(
            summary = "📊 إحصائيات السجلات الطبية",
            description = """
            الحصول على إحصائيات شاملة للسجلات الطبية:
            - عدد السجلات حسب الحالة
            - إحصائيات زمنية (اليوم، الأسبوع، الشهر)
            - أكثر التشخيصات شيوعاً
            - الأدوية الأكثر وصفاً
            - توزيع أنواع الزيارات
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على الإحصائيات بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<MedicalRecordStatisticsResponse>> getMedicalRecordStatistics(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            MedicalRecordStatisticsResponse statistics =
                    medicalRecordService.getMedicalRecordStatistics(currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على إحصائيات السجلات الطبية بنجاح", statistics)
            );
        } catch (Exception e) {
            log.error("خطأ في جلب إحصائيات السجلات الطبية: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في جلب الإحصائيات: " + e.getMessage(), null));
        }
    }

    /**
     * تصدير السجل الطبي كملف PDF
     */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "📄 تصدير السجل الطبي كـ PDF",
            description = """
            تصدير السجل الطبي كملف PDF للطباعة أو الأرشفة:
            - تصميم احترافي مناسب للطباعة
            - يتضمن جميع تفاصيل السجل
            - معلومات العيادة والطبيب والمريض
            - العلامات الحيوية والتشخيص والعلاج
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "تم إنشاء ملف PDF بنجاح"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "السجل الطبي غير موجود"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "غير مصرح بالوصول لهذا السجل"
            )
    })
    public ResponseEntity<byte[]> exportMedicalRecordPdf(
            @Parameter(description = "معرف السجل الطبي", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {

        log.info("تصدير السجل الطبي {} كـ PDF بواسطة المستخدم {}", id, currentUser.getUsername());

        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                log.info("مدير النظام يصدر السجل الطبي {} من العيادة: {}",
                        id, clinicId != null ? clinicId : "جميع العيادات");
            }

            // Get medical record
            MedicalRecordResponse medicalRecord = medicalRecordService.getMedicalRecordById(id, currentUser);

            // Generate PDF
            byte[] pdfBytes = pdfMedicalRecordService.generateMedicalRecordPdf(medicalRecord);

            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "medical-record-" + id + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("خطأ في تصدير السجل الطبي {} كـ PDF: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}