// =============================================================================
// Appointment Controller - وحدة التحكم بالمواعيد
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.annotation.SystemAdminContext;
import com.nakqeeb.amancare.dto.request.CreateAppointmentRequest;
import com.nakqeeb.amancare.dto.request.UpdateAppointmentRequest;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.entity.AppointmentStatus;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.AppointmentService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * وحدة التحكم بالمواعيد
 */
@RestController
@RequestMapping("/appointments")
@Tag(name = "📅 إدارة المواعيد", description = "APIs الخاصة بإدارة مواعيد العيادة")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AppointmentController {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;

    /**
     * إنشاء موعد جديد
     */
    @PostMapping
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "➕ إنشاء موعد جديد",
            description = "حجز موعد جديد للمريض مع الطبيب مع التحقق من التعارض",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "مثال إنشاء موعد",
                                    value = """
                        {
                          "patientId": 1,
                          "doctorId": 2,
                          "appointmentDate": "2024-08-28",
                          "appointmentTime": "10:30:00",
                          "durationMinutes": 30,
                          "appointmentType": "CONSULTATION",
                          "chiefComplaint": "ألم في الصدر وصعوبة في التنفس",
                          "notes": "المريض يشكو من الأعراض منذ أسبوعين"
                        }
                        """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "تم إنشاء الموعد بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "بيانات غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "المريض أو الطبيب غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "يوجد تعارض مع موعد آخر"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateAppointmentRequest request) {
        try {
            AppointmentResponse appointment = appointmentService.createAppointment(
                    currentUser.getClinicId(), currentUser.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "تم إنشاء الموعد بنجاح", appointment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في إنشاء الموعد: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على جميع المواعيد مع التصفية
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "📋 قائمة المواعيد",
            description = "الحصول على قائمة المواعيد مع إمكانية التصفية بالتاريخ والطبيب والحالة"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على قائمة المواعيد بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<AppointmentPageResponse>> getAllAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "تاريخ المواعيد", example = "2024-08-28")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "معرف الطبيب", example = "2")
            @RequestParam(required = false) Long doctorId,
            @Parameter(description = "حالة الموعد", example = "SCHEDULED")
            @RequestParam(required = false) AppointmentStatus status,
            @Parameter(description = "رقم الصفحة (يبدأ من 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ترتيب حسب", example = "appointmentDate")
            @RequestParam(defaultValue = "appointmentDate") String sortBy,
            @Parameter(description = "اتجاه الترتيب", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading appointments from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            AppointmentPageResponse appointments = appointmentService.getAllAppointments(
                    effectiveClinicId, date, doctorId, status, page, size, sortBy, sortDirection
            );
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على قائمة المواعيد بنجاح", appointments)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على قائمة المواعيد: " + e.getMessage(), null));
        }
    }

    /**
     * مواعيد اليوم
     */
    @GetMapping("/today")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "📅 مواعيد اليوم",
            description = "الحصول على جميع المواعيد المجدولة لليوم الحالي"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على مواعيد اليوم بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<AppointmentSummaryResponse>>> getTodayAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading today's appointments from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            List<AppointmentSummaryResponse> todayAppointments = appointmentService.getTodayAppointments(
                    effectiveClinicId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على مواعيد اليوم بنجاح", todayAppointments)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على مواعيد اليوم: " + e.getMessage(), null));
        }
    }

    /**
     * مواعيد طبيب معين
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "👨‍⚕️ مواعيد الطبيب",
            description = "الحصول على مواعيد طبيب معين في تاريخ محدد أو اليوم الحالي"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على مواعيد الطبيب بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الطبيب غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<AppointmentSummaryResponse>>> getDoctorAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "معرف الطبيب", example = "2")
            @PathVariable Long doctorId,
            @Parameter(description = "التاريخ (إذا لم يتم تحديده، سيتم عرض مواعيد اليوم)", example = "2024-08-28")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading a doctor's appointments from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            List<AppointmentSummaryResponse> doctorAppointments = appointmentService.getDoctorAppointments(
                    effectiveClinicId, doctorId, date);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على مواعيد الطبيب بنجاح", doctorAppointments)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في الحصول على مواعيد الطبيب: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على موعد محدد
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "🔍 تفاصيل الموعد",
            description = "الحصول على تفاصيل موعد محدد"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على تفاصيل الموعد بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الموعد غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "معرف الموعد", example = "1")
            @PathVariable Long id) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading a specific appointment from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            AppointmentResponse appointment = appointmentService.getAppointmentById(effectiveClinicId, id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على تفاصيل الموعد بنجاح", appointment)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "الموعد غير موجود: " + e.getMessage(), null));
        }
    }

    /**
     * تحديث موعد
     */
    @PutMapping("/{id}")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "✏️ تحديث الموعد",
            description = "تحديث تفاصيل موعد موجود (التاريخ، الوقت، النوع، الملاحظات)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "مثال تحديث موعد",
                                    value = """
                        {
                          "appointmentDate": "2024-08-29",
                          "appointmentTime": "14:30:00",
                          "durationMinutes": 45,
                          "appointmentType": "FOLLOW_UP",
                          "chiefComplaint": "متابعة الحالة والتحسن",
                          "notes": "المريض يشعر بتحسن ملحوظ منذ الزيارة الأخيرة"
                        }
                        """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم تحديث الموعد بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "بيانات غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الموعد غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "يوجد تعارض مع موعد آخر"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointment(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف الموعد", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        try {
            AppointmentResponse appointment = appointmentService.updateAppointment(
                    currentUser.getClinicId(), id, request);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تحديث الموعد بنجاح", appointment)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في تحديث الموعد: " + e.getMessage(), null));
        }
    }

    /**
     * تحديث حالة الموعد
     */
    @PatchMapping("/{id}/status")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "🔄 تحديث حالة الموعد",
            description = "تغيير حالة الموعد (مجدول، مؤكد، قيد التنفيذ، مكتمل، ملغي، لم يحضر)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم تحديث حالة الموعد بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "انتقال غير صحيح للحالة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الموعد غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف الموعد", example = "1")
            @PathVariable Long id,
            @Parameter(description = "الحالة الجديدة", example = "COMPLETED")
            @RequestParam AppointmentStatus status) {
        try {
            AppointmentResponse appointment = appointmentService.updateAppointmentStatus(
                    currentUser.getClinicId(), id, status);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تحديث حالة الموعد بنجاح", appointment)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في تحديث حالة الموعد: " + e.getMessage(), null));
        }
    }

    /**
     * إلغاء موعد
     */
    @DeleteMapping("/{id}")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "❌ إلغاء الموعد",
            description = "إلغاء موعد مع إمكانية إضافة سبب الإلغاء"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم إلغاء الموعد بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "لا يمكن إلغاء الموعد"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الموعد غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف الموعد", example = "1")
            @PathVariable Long id,
            @Parameter(description = "سبب الإلغاء", example = "المريض غير قادر على الحضور")
            @RequestParam(required = false) String reason) {
        try {
            appointmentService.cancelAppointment(currentUser.getClinicId(), id, reason);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم إلغاء الموعد بنجاح", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في إلغاء الموعد: " + e.getMessage(), null));
        }
    }

    /**
     * المواعيد القادمة للمريض
     */
    @GetMapping("/patient/{patientId}/upcoming")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "⏭️ المواعيد القادمة للمريض",
            description = "الحصول على المواعيد القادمة لمريض معين"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على المواعيد القادمة بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "المريض غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<AppointmentSummaryResponse>>> getUpcomingAppointmentsByPatient(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "معرف المريض", example = "1")
            @PathVariable Long patientId) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading upcoming appointments for a specific patients from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            List<AppointmentSummaryResponse> upcomingAppointments = appointmentService.getUpcomingAppointmentsByPatient(
                    effectiveClinicId, patientId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على المواعيد القادمة بنجاح", upcomingAppointments)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في الحصول على المواعيد القادمة: " + e.getMessage(), null));
        }
    }

    /**
     * المواعيد المتأخرة
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR')")
    @Operation(
            summary = "⏰ المواعيد المتأخرة",
            description = "الحصول على المواعيد التي فات موعدها ولم يتم إكمالها أو إلغاؤها"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على المواعيد المتأخرة بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - مدير العيادة أو الطبيب فقط")
    })
    public ResponseEntity<ApiResponse<List<AppointmentSummaryResponse>>> getOverdueAppointments(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading past, uncompleted, or canceled appointments from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            List<AppointmentSummaryResponse> overdueAppointments = appointmentService.getOverdueAppointments(
                    effectiveClinicId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على المواعيد المتأخرة بنجاح", overdueAppointments)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على المواعيد المتأخرة: " + e.getMessage(), null));
        }
    }

    /**
     * إحصائيات المواعيد
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR')")
    @Operation(
            summary = "📊 إحصائيات المواعيد",
            description = "الحصول على إحصائيات المواعيد لتاريخ معين أو اليوم الحالي"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على الإحصائيات بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - مدير العيادة أو الطبيب فقط")
    })
    public ResponseEntity<ApiResponse<AppointmentStatistics>> getAppointmentStatistics(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "التاريخ (إذا لم يتم تحديده، سيتم عرض إحصائيات اليوم)", example = "2024-08-28")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading appointments statistics from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            AppointmentStatistics statistics = appointmentService.getAppointmentStatistics(
                    effectiveClinicId, date);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على الإحصائيات بنجاح", statistics)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على الإحصائيات: " + e.getMessage(), null));
        }
    }
}