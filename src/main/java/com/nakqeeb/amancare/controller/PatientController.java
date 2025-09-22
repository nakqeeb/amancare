// =============================================================================
// Patient Controller - وحدة التحكم بالمرضى
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.annotation.SystemAdminContext;
import com.nakqeeb.amancare.dto.request.CreatePatientRequest;
import com.nakqeeb.amancare.dto.request.UpdatePatientRequest;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.entity.BloodType;
import com.nakqeeb.amancare.entity.Gender;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.ClinicContextService;
import com.nakqeeb.amancare.service.PatientService;
import com.nakqeeb.amancare.service.pdf.PdfPatientService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * وحدة التحكم بالمرضى
 */
@RestController
@RequestMapping("/patients")
@Tag(name = "👥 إدارة المرضى", description = "APIs الخاصة بإدارة بيانات المرضى")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PatientController {
    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    private PatientService patientService;

    @Autowired
    private ClinicContextService clinicContextService;

    @Autowired
    private PdfPatientService pdfPatientService;

    /**
     * إنشاء مريض جديد
     */
    @PostMapping
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "➕ إنشاء مريض جديد",
            description = """
            إضافة مريض جديد إلى العيادة مع توليد رقم مريض تلقائي :
            - SYSTEM_ADMIN: يجب تفعيل سياق العيادة أولاً
            - باقي الأدوار: يضيفون في عيادتهم مباشرة
            
            Headers المطلوبة لـ SYSTEM_ADMIN:
            - X-Acting-Clinic-Id: معرف العيادة
            - X-Acting-Reason: سبب العملية
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "مثال إنشاء مريض",
                                    value = """
                        {
                          "firstName": "محمد",
                          "lastName": "أحمد علي",
                          "dateOfBirth": "1985-03-15",
                          "gender": "MALE",
                          "phone": "771234567",
                          "email": "mohammed.ahmed@example.com",
                          "address": "حي الصافية، شارع الجامعة، صنعاء، اليمن",
                          "emergencyContactName": "فاطمة أحمد",
                          "emergencyContactPhone": "773456789",
                          "bloodType": "O_POSITIVE",
                          "allergies": "حساسية من البنسلين والأسبرين",
                          "chronicDiseases": "ارتفاع ضغط الدم الخفيف",
                          "notes": "يفضل المواعيد الصباحية، لديه خوف من الحقن"
                        }
                        """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "تم إنشاء المريض بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "بيانات غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "رقم الهاتف مستخدم بالفعل")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreatePatientRequest request) {
        try {
            // Log if SYSTEM_ADMIN is acting with context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                ClinicContextService.ClinicContextInfo contextInfo =
                        clinicContextService.getCurrentContext(currentUser);
                logger.info("SYSTEM_ADMIN is creating a patient with clinic context. ActingClinicId: {}, Reason: {}",
                        contextInfo.getActingAsClinicId(), contextInfo.getReason());
            }

            PatientResponse patient = patientService.createPatient(currentUser, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "تم إنشاء المريض بنجاح", patient));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في إنشاء المريض: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على جميع المرضى مع ترقيم الصفحات
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "📋 قائمة المرضى",
            description = "الحصول على قائمة المرضى مع دعم ترقيم الصفحات والترتيب"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على قائمة المرضى بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<PatientPageResponse>> getAllPatients(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "رقم الصفحة (يبدأ من 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ترتيب حسب", example = "firstName")
            @RequestParam(defaultValue = "firstName") String sortBy,
            @Parameter(description = "اتجاه الترتيب", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading patients from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            PatientPageResponse patients = patientService.getAllPatients(
                    effectiveClinicId, page, size, sortBy, sortDirection
            );
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على قائمة المرضى بنجاح", patients)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على قائمة المرضى: " + e.getMessage(), null));
        }
    }

    /**
     * Enhanced search endpoint with multiple filters
     * البحث المحسن في المرضى مع فلاتر متعددة
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "🔍 البحث المحسن في المرضى",
            description = "البحث في المرضى بالاسم أو رقم الهاتف أو رقم المريض مع إمكانية التصفية حسب الجنس وفصيلة الدم والحالة"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم البحث بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<PatientPageResponse>> searchPatients(
            @AuthenticationPrincipal UserPrincipal currentUser,

            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,

            @Parameter(description = "كلمة البحث (الاسم، الهاتف، رقم المريض)", example = "محمد")
            @RequestParam(required = false) String q,

            @Parameter(description = "تصفية حسب الجنس")
            @RequestParam(required = false) Gender gender,

            @Parameter(description = "تصفية حسب فصيلة الدم")
            @RequestParam(required = false) BloodType bloodType,

            @Parameter(description = "تصفية حسب الحالة (نشط/غير نشط)")
            @RequestParam(required = false) Boolean isActive,

            @Parameter(description = "رقم الصفحة", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        try {
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                effectiveClinicId = clinicId;
                logger.info("SYSTEM_ADMIN searching patients from clinic: {} with filters - gender: {}, bloodType: {}, isActive: {}",
                        clinicId != null ? clinicId : "ALL", gender, bloodType, isActive);
            } else {
                effectiveClinicId = currentUser.getClinicId();
            }

            // Call the enhanced search method
            PatientPageResponse patients = patientService.searchPatients(
                    effectiveClinicId, q, gender, bloodType, isActive, page, size
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم البحث بنجاح", patients)
            );
        } catch (Exception e) {
            logger.error("Error searching patients: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في البحث: " + e.getMessage(), null));
        }
    }


    /**
     * Legacy search endpoint - kept for backward compatibility
     * نقطة النهاية القديمة للبحث - محفوظة للتوافق العكسي
     */
    /**
     * البحث في المرضى
     */
    @GetMapping("/search/legacy")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "🔍 البحث في المرضى",
            description = "البحث في المرضى بالاسم أو رقم الهاتف أو رقم المريض"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم البحث بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<PatientPageResponse>> searchPatientsLegacy(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "كلمة البحث (الاسم، الهاتف، رقم المريض)", example = "محمد")
            @RequestParam(required = false) String q,
            @Parameter(description = "رقم الصفحة", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading searched patients from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            PatientPageResponse patients = patientService.searchPatientsLegacy(
                    effectiveClinicId, q, page, size
            );
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم البحث بنجاح", patients)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في البحث: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على مريض بالمعرف
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "👤 تفاصيل المريض",
            description = "الحصول على تفاصيل مريض محدد بالمعرف"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على تفاصيل المريض بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "المريض غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "معرف المريض", example = "1")
            @PathVariable Long id) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading a patient from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            PatientResponse patient = patientService.getPatientById(effectiveClinicId, id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على تفاصيل المريض بنجاح", patient)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "المريض غير موجود: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على مريض برقم المريض
     */
    @GetMapping("/number/{patientNumber}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "🔢 البحث برقم المريض",
            description = "الحصول على مريض باستخدام رقم المريض"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم العثور على المريض بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "المريض غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientByNumber(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "رقم المريض", example = "P202401001")
            @PathVariable String patientNumber) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading patient from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            PatientResponse patient = patientService.getPatientByNumber(effectiveClinicId, patientNumber);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم العثور على المريض بنجاح", patient)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "المريض غير موجود: " + e.getMessage(), null));
        }
    }

    /**
     * تحديث بيانات مريض
     */
    @PutMapping("/{id}")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "✏️ تحديث بيانات المريض",
            description = "تحديث بيانات مريض موجود (يتم تحديث الحقول المُرسلة فقط)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "مثال تحديث بيانات المريض",
                                    value = """
                        {
                          "phone": "775555555",
                          "email": "mohammed.new@example.com",
                          "address": "حي الحصبة، شارع الستين، صنعاء، اليمن",
                          "allergies": "حساسية من البنسلين والمكسرات والأسبرين",
                          "notes": "يفضل المواعيد المسائية، تم تعديل العنوان"
                        }
                        """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم تحديث بيانات المريض بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "المريض غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "بيانات غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "رقم الهاتف مستخدم بالفعل")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف المريض", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdatePatientRequest request) {
        try {
            // Log if SYSTEM_ADMIN is acting with context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                ClinicContextService.ClinicContextInfo contextInfo =
                        clinicContextService.getCurrentContext(currentUser);
                logger.info("SYSTEM_ADMIN is updating a patient with clinic context. ActingClinicId: {}, Reason: {}",
                        contextInfo.getActingAsClinicId(), contextInfo.getReason());
            }
            PatientResponse patient = patientService.updatePatient(currentUser, id, request);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تحديث بيانات المريض بنجاح", patient)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في تحديث بيانات المريض: " + e.getMessage(), null));
        }
    }

    /**
     * حذف مريض (إلغاء تفعيل)
     */
    @DeleteMapping("/{id}")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR')")
    @Operation(
            summary = "🗑️ حذف المريض",
            description = "حذف مريض (إلغاء تفعيل - لا يتم الحذف النهائي للحفاظ على السجلات)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم حذف المريض بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "المريض غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - مدير العيادة أو الطبيب فقط")
    })
    public ResponseEntity<ApiResponse<Void>> deletePatient(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف المريض", example = "1")
            @PathVariable Long id) {
        try {
            // Log if SYSTEM_ADMIN is acting with context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                ClinicContextService.ClinicContextInfo contextInfo =
                        clinicContextService.getCurrentContext(currentUser);
                logger.info("SYSTEM_ADMIN is deactivating a patient with clinic context. ActingClinicId: {}, Reason: {}",
                        contextInfo.getActingAsClinicId(), contextInfo.getReason());
            }
            patientService.deletePatient(currentUser, id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم حذف المريض بنجاح", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في حذف المريض: " + e.getMessage(), null));
        }
    }

    /**
     * إعادة تفعيل مريض
     */
    @PostMapping("/{id}/reactivate")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN')")
    @Operation(
            summary = "🔄 إعادة تفعيل المريض",
            description = "إعادة تفعيل مريض محذوف (مدير العيادة فقط)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم إعادة تفعيل المريض بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "المريض غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - مدير العيادة فقط")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> reactivatePatient(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف المريض", example = "1")
            @PathVariable Long id) {
        try {
            // Log if SYSTEM_ADMIN is acting with context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                ClinicContextService.ClinicContextInfo contextInfo =
                        clinicContextService.getCurrentContext(currentUser);
                logger.info("SYSTEM_ADMIN is reactivating a patient with clinic context. ActingClinicId: {}, Reason: {}",
                        contextInfo.getActingAsClinicId(), contextInfo.getReason());
            }
            PatientResponse patient = patientService.reactivatePatient(currentUser, id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم إعادة تفعيل المريض بنجاح", patient)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في إعادة تفعيل المريض: " + e.getMessage(), null));
        }
    }

    /**
     * حذف مريض نهائياً - SYSTEM_ADMIN فقط
     * WARNING: This permanently deletes all patient data and cannot be undone
     */
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "⚠️ حذف المريض نهائياً",
            description = "حذف المريض نهائياً من قاعدة البيانات (مدير النظام فقط) - تحذير: لا يمكن التراجع عن هذا الإجراء"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "تم حذف المريض نهائياً بنجاح"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "المريض غير موجود"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "غير مصرح - يجب تسجيل الدخول"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ممنوع - مدير النظام فقط"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "لا يمكن حذف المريض - يحتوي على سجلات مرتبطة نشطة"
            )
    })
    public ResponseEntity<ApiResponse<PermanentDeleteResponse>> permanentlyDeletePatient(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف المريض", example = "1")
            @PathVariable Long id,
            @Parameter(description = "رمز التأكيد للحذف النهائي", example = "DELETE-CONFIRM")
            @RequestParam(required = true) String confirmationCode) {

        // Verify confirmation code
        if (!"DELETE-CONFIRM".equals(confirmationCode)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false,
                            "رمز التأكيد غير صحيح. يجب إدخال DELETE-CONFIRM للمتابعة", null));
        }

        try {
            // Log this critical action
            logger.warn("PERMANENT DELETE: User {} (ID: {}) is permanently deleting patient ID: {}",
                    currentUser.getUsername(), currentUser.getId(), id);

            PermanentDeleteResponse response = patientService.permanentlyDeletePatient(
                    currentUser.getClinicId(), id, currentUser.getId());

            return ResponseEntity.ok(
                    new ApiResponse<>(true,
                            "تم حذف المريض نهائياً. تم حذف جميع البيانات المرتبطة.", response)
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false,
                            "لا يمكن حذف المريض: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error permanently deleting patient {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false,
                            "فشل في حذف المريض نهائياً: " + e.getMessage(), null));
        }
    }

    /**
     * إحصائيات المرضى
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR')")
    @Operation(
            summary = "📊 إحصائيات المرضى",
            description = "الحصول على إحصائيات المرضى في العيادة"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على الإحصائيات بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - مدير العيادة أو الطبيب فقط")
    })
    public ResponseEntity<ApiResponse<PatientStatistics>> getPatientStatistics(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading patient statistics from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            PatientStatistics statistics = patientService.getPatientStatisticsSimple(effectiveClinicId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على الإحصائيات بنجاح", statistics)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على الإحصائيات: " + e.getMessage(), null));
        }
    }

    /**
     * المرضى الذين لديهم مواعيد اليوم
     */
    @GetMapping("/today")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "📅 مرضى اليوم",
            description = "الحصول على قائمة المرضى الذين لديهم مواعيد اليوم"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على مرضى اليوم بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<PatientSummaryResponse>>> getTodayPatients(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading today's patients from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            List<PatientSummaryResponse> todayPatients = patientService.getTodayPatients(effectiveClinicId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على مرضى اليوم بنجاح", todayPatients)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على مرضى اليوم: " + e.getMessage(), null));
        }
    }

    /**
     * تصدير تفاصيل المريض كـ PDF
     * Export patient details as PDF
     */
    @GetMapping("/{id}/export/pdf")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "📄 تصدير تفاصيل المريض PDF",
            description = """
        تصدير تفاصيل المريض الكاملة في ملف PDF مع دعم اللغة العربية
        - تصميم احترافي مع شعار الشركة
        - دعم كامل للغة العربية و RTL
        - معلومات المريض الكاملة
        """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "تم تصدير PDF بنجاح",
                            content = @Content(mediaType = "application/pdf")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "المريض غير موجود"
                    )
            }
    )
    public ResponseEntity<byte[]> exportPatientDetailsPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {

        logger.info("Exporting patient {} details as PDF by user {}", id, currentUser.getUsername());

        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN Exporting patient details from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }

            // Fetch patient details
            PatientResponse patient = patientService.getPatientById(effectiveClinicId, id);

            // Generate PDF
            byte[] pdfContent = pdfPatientService.generatePatientDetailsPdf(patient);

            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = String.format("patient_%s_%s.pdf",
                    patient.getPatientNumber(),
                    LocalDate.now().format(DateTimeFormatter.ISO_DATE));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);

        } catch (ResourceNotFoundException e) {
            logger.error("Patient not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error exporting patient PDF: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * تصدير بطاقة المريض كـ PDF
     * Export patient card as PDF
     */
    @GetMapping("/{id}/export/card")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "🎫 تصدير بطاقة المريض PDF",
            description = """
        تصدير بطاقة المريض المختصرة في ملف PDF
        - حجم A5 أفقي
        - تصميم بطاقة احترافي
        - معلومات أساسية ومعلومات الطوارئ
        """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "تم تصدير البطاقة بنجاح",
                            content = @Content(mediaType = "application/pdf")
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "المريض غير موجود"
                    )
            }
    )
    public ResponseEntity<byte[]> exportPatientCardPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {

        logger.info("Exporting patient {} card as PDF by user {}", id, currentUser.getUsername());

        try {

            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN Exporting patient card as PDF from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }

            // Fetch patient details
            PatientResponse patient = patientService.getPatientById(effectiveClinicId, id);

            // Generate PDF card
            byte[] pdfContent = pdfPatientService.generatePatientCardPdf(patient);

            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = String.format("patient_card_%s.pdf", patient.getPatientNumber());
            headers.setContentDispositionFormData("attachment", filename);
//            headers.setContentDispositionFormData("inline", filename); // inline for quick preview
//            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);

        } catch (ResourceNotFoundException e) {
            logger.error("Patient not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error exporting patient card PDF: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * تصدير قائمة المرضى كـ PDF
     * Export patients list as PDF (Optional - for future enhancement)
     */
    @GetMapping("/export/list")
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "📋 تصدير قائمة المرضى PDF",
            description = """
        تصدير قائمة المرضى في ملف PDF
        - جدول بالمرضى النشطين
        - معلومات أساسية لكل مريض
        """,
            parameters = {
                    @Parameter(name = "page", description = "رقم الصفحة", example = "0"),
                    @Parameter(name = "size", description = "حجم الصفحة", example = "50"),
                    @Parameter(name = "searchTerm", description = "كلمة البحث"),
                    @Parameter(name = "isActive", description = "حالة النشاط")
            }
    )
    public ResponseEntity<byte[]> exportPatientsListPdf(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean isActive,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        logger.info("Exporting patients list as PDF by user {}", currentUser.getUsername());

        // Implementation for list export (if needed)
        // This can be implemented later based on requirements

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .body(new byte[0]); // Placeholder
    }
}