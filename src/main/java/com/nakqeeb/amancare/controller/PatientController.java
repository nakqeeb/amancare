// =============================================================================
// Patient Controller - وحدة التحكم بالمرضى
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.request.CreatePatientRequest;
import com.nakqeeb.amancare.dto.request.UpdatePatientRequest;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.PatientService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    /**
     * إنشاء مريض جديد
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "➕ إنشاء مريض جديد",
            description = "إضافة مريض جديد إلى العيادة مع توليد رقم مريض تلقائي",
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
            PatientResponse patient = patientService.createPatient(currentUser.getClinicId(), request);
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
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
            @Parameter(description = "رقم الصفحة (يبدأ من 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ترتيب حسب", example = "firstName")
            @RequestParam(defaultValue = "firstName") String sortBy,
            @Parameter(description = "اتجاه الترتيب", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            PatientPageResponse patients = patientService.getAllPatients(
                    currentUser.getClinicId(), page, size, sortBy, sortDirection
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
     * البحث في المرضى
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "🔍 البحث في المرضى",
            description = "البحث في المرضى بالاسم أو رقم الهاتف أو رقم المريض"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم البحث بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<PatientPageResponse>> searchPatients(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "كلمة البحث (الاسم، الهاتف، رقم المريض)", example = "محمد")
            @RequestParam(required = false) String q,
            @Parameter(description = "رقم الصفحة", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        try {
            PatientPageResponse patients = patientService.searchPatients(
                    currentUser.getClinicId(), q, page, size
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
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
            @Parameter(description = "معرف المريض", example = "1")
            @PathVariable Long id) {
        try {
            PatientResponse patient = patientService.getPatientById(currentUser.getClinicId(), id);
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
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
            @Parameter(description = "رقم المريض", example = "P202401001")
            @PathVariable String patientNumber) {
        try {
            PatientResponse patient = patientService.getPatientByNumber(currentUser.getClinicId(), patientNumber);
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
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
            PatientResponse patient = patientService.updatePatient(currentUser.getClinicId(), id, request);
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
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
            patientService.deletePatient(currentUser.getClinicId(), id);
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
    @PreAuthorize("hasRole('ADMIN')")
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
            PatientResponse patient = patientService.reactivatePatient(currentUser.getClinicId(), id);
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
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
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            PatientStatistics statistics = patientService.getPatientStatistics(currentUser.getClinicId());
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
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
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            List<PatientSummaryResponse> todayPatients = patientService.getTodayPatients(currentUser.getClinicId());
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على مرضى اليوم بنجاح", todayPatients)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على مرضى اليوم: " + e.getMessage(), null));
        }
    }
}