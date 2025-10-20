// =============================================================================
// Doctor Schedule Controller - وحدة التحكم بجدولة الأطباء
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.annotation.SystemAdminContext;
import com.nakqeeb.amancare.dto.request.CreateDoctorScheduleRequest;
import com.nakqeeb.amancare.dto.request.CreateUnavailabilityRequest;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.entity.DoctorSchedule;
import com.nakqeeb.amancare.entity.DoctorUnavailability;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.repository.UserRepository;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.AppointmentTokenService;
import com.nakqeeb.amancare.service.DoctorScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * وحدة التحكم بجدولة الأطباء
 */
@RestController
@RequestMapping("/schedules")
@Tag(name = "🗓️ جدولة الأطباء", description = "APIs الخاصة بإدارة جداول ومواعيد عمل الأطباء")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DoctorScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(DoctorScheduleController.class);

    @Autowired
    private DoctorScheduleService scheduleService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppointmentTokenService tokenService;
    /**
     * إنشاء جدولة جديدة للطبيب
     */
    @PostMapping("/doctor")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR')")
    @Operation(
            summary = "➕ إنشاء جدولة طبيب",
            description = "إنشاء جدول عمل جديد للطبيب مع تحديد الأيام والأوقات",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "جدول عمل 5 أيام",
                                            value = """
                            {
                              "doctorId": 2,
                              "workingDays": ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"],
                              "startTime": "08:00:00",
                              "endTime": "16:00:00",
                              "breakStartTime": "12:00:00",
                              "breakEndTime": "13:00:00",
                              "effectiveDate": "2024-09-01",
                              "scheduleType": "REGULAR",
                              "notes": "جدول العمل الاعتيادي - الأحد إلى الخميس"
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "جدول عمل جزئي",
                                            value = """
                            {
                              "doctorId": 2,
                              "workingDays": ["SUNDAY", "TUESDAY", "THURSDAY"],
                              "startTime": "10:00:00",
                              "endTime": "14:00:00",
                              "effectiveDate": "2024-09-01",
                              "scheduleType": "REGULAR",
                              "notes": "جدول عمل جزئي - 3 أيام في الأسبوع"
                            }
                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "تم إنشاء الجدولة بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "بيانات غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الطبيب غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponse>>> createDoctorSchedule(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateDoctorScheduleRequest request) {
        try {
            List<DoctorSchedule> schedules = scheduleService.createDoctorSchedule(
                    currentUser.getClinicId(), request);

            List<DoctorScheduleResponse> responses = schedules.stream()
                    .map(DoctorScheduleResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "تم إنشاء جدولة الطبيب بنجاح", responses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في إنشاء الجدولة: " + e.getMessage(), null));
        }
    }

    /**
     * إضافة وقت عدم توفر للطبيب
     */
    @PostMapping("/unavailability")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR')")
    @Operation(
            summary = "🚫 إضافة عدم توفر",
            description = "إضافة وقت عدم توفر للطبيب (إجازة، مؤتمر، طوارئ)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "إجازة يوم كامل",
                                            value = """
                            {
                              "doctorId": 2,
                              "unavailableDate": "2024-09-15",
                              "unavailabilityType": "VACATION",
                              "reason": "إجازة سنوية",
                              "isRecurring": false
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "عدم توفر جزئي",
                                            value = """
                            {
                              "doctorId": 2,
                              "unavailableDate": "2024-09-20",
                              "startTime": "10:00:00",
                              "endTime": "14:00:00",
                              "unavailabilityType": "CONFERENCE",
                              "reason": "مؤتمر طبي"
                            }
                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "تم إضافة عدم التوفر بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "بيانات غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الطبيب غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<UnavailabilityResponse>> addUnavailability(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateUnavailabilityRequest request) {
        try {
            DoctorUnavailability unavailability = scheduleService.addUnavailability(
                    currentUser.getClinicId(), request);

            UnavailabilityResponse response = UnavailabilityResponse.fromEntity(unavailability);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "تم إضافة عدم التوفر بنجاح", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في إضافة عدم التوفر: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على جدولة طبيب
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "📅 جدولة الطبيب",
            description = "الحصول على جدول عمل طبيب معين"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على الجدولة بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الطبيب غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponse>>> getDoctorSchedule(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "معرف الطبيب", example = "2")
            @PathVariable Long doctorId) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading availability times for a specific doctor from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            List<DoctorSchedule> schedules = scheduleService.getDoctorSchedule(
                    effectiveClinicId, doctorId);

            List<DoctorScheduleResponse> responses = schedules.stream()
                    .map(DoctorScheduleResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على جدولة الطبيب بنجاح", responses)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في الحصول على الجدولة: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على عدم توفر الطبيب
     */
    @GetMapping("/doctor/{doctorId}/unavailability")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "🚫 أوقات عدم التوفر",
            description = "الحصول على أوقات عدم توفر طبيب في فترة زمنية معينة"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على أوقات عدم التوفر بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الطبيب غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<UnavailabilityResponse>>> getDoctorUnavailability(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "معرف الطبيب", example = "2")
            @PathVariable Long doctorId,
            @Parameter(description = "تاريخ البداية", example = "2024-09-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "تاريخ النهاية", example = "2024-09-30")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading unavailability times for a specific doctor from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            LocalDate start = startDate != null ? startDate : LocalDate.now();
            LocalDate end = endDate != null ? endDate : start.plusMonths(1);

            List<DoctorUnavailability> unavailabilities = scheduleService.getDoctorUnavailability(
                    effectiveClinicId, doctorId, start, end);

            List<UnavailabilityResponse> responses = unavailabilities.stream()
                    .map(UnavailabilityResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على أوقات عدم التوفر بنجاح", responses)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في الحصول على أوقات عدم التوفر: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على الأوقات المتاحة للطبيب في تاريخ معين
     */
    @GetMapping("/doctor/{doctorId}/available-slots")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "🕐 الأوقات المتاحة",
            description = "الحصول على الأوقات المتاحة للطبيب في تاريخ معين لحجز المواعيد"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على الأوقات المتاحة بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الطبيب غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<LocalTime>>> getAvailableTimeSlots(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "معرف الطبيب", example = "2")
            @PathVariable Long doctorId,
            @Parameter(description = "التاريخ المطلوب", example = "2024-09-15", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "مدة الموعد بالدقائق", example = "30")
            @RequestParam(defaultValue = "30") int durationMinutes) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading available-slots for a specific doctor at specific time from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            List<LocalTime> availableSlots = scheduleService.getAvailableTimeSlots(
                    effectiveClinicId, doctorId, date, durationMinutes);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على الأوقات المتاحة بنجاح", availableSlots)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في الحصول على الأوقات المتاحة: " + e.getMessage(), null));
        }
    }

    /**
     * Get available time slots with token numbers
     */
    @GetMapping("/doctor/{doctorId}/available-slots-with-tokens")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    @Operation(
            summary = "🎫 الأوقات المتاحة مع أرقام الرموز",
            description = "الحصول على الأوقات المتاحة مع أرقام الرموز المقابلة لها"
    )
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getAvailableTimeSlotsWithTokens(
            @AuthenticationPrincipal UserPrincipal currentUser,
            /* @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,*/
            @Parameter(description = "معرف الطبيب", example = "2")
            @PathVariable Long doctorId,
            @Parameter(description = "التاريخ المطلوب", example = "2025-01-15", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "مدة الموعد بالدقائق", example = "30")
            @RequestParam(defaultValue = "30") int durationMinutes) {
        try {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

            Map<LocalTime, Integer> availableSlots = tokenService.getAvailableTimeSlotsWithTokens(
                    doctor, date, durationMinutes
            );

            // Convert LocalTime to String for JSON
            Map<String, Integer> result = new LinkedHashMap<>();
            availableSlots.forEach((time, token) -> result.put(time.toString(), token));

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على الأوقات المتاحة مع الرموز بنجاح", result)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في الحصول على الأوقات المتاحة: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على الأطباء المتاحين في وقت معين
     */
    @GetMapping("/available-doctors")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "👨‍⚕️ الأطباء المتاحون",
            description = "الحصول على قائمة الأطباء المتاحين في تاريخ ووقت معين"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على قائمة الأطباء المتاحين بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<DoctorSummaryResponse>>> getAvailableDoctors(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "التاريخ المطلوب", example = "2024-09-15", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            // @Parameter(description = "الوقت المطلوب", example = "10:30:00", required = true)
            @Parameter(in = ParameterIn.QUERY, name = "time",
                    schema = @Schema(type = "string", description = "الوقت المطلوب" , example = "10:30:00"))
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading available-doctors at specific time from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            List<User> availableDoctors = scheduleService.getAvailableDoctors(
                    effectiveClinicId, date, time);

            List<DoctorSummaryResponse> responses = availableDoctors.stream()
                    .map(doctor -> {
                        DoctorSummaryResponse response = new DoctorSummaryResponse();
                        response.setId(doctor.getId());
                        response.setFullName(doctor.getFullName());
                        response.setSpecialization(doctor.getSpecialization());
                        return response;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على قائمة الأطباء المتاحين بنجاح", responses)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في الحصول على الأطباء المتاحين: " + e.getMessage(), null));
        }
    }

    /**
     * التحقق من توفر الطبيب
     */
    @GetMapping("/doctor/{doctorId}/availability")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "✅ التحقق من التوفر",
            description = "التحقق من توفر طبيب معين في تاريخ ووقت محدد"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم التحقق من التوفر بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الطبيب غير موجود"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<AvailabilityCheckResponse>> checkDoctorAvailability(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف الطبيب", example = "2")
            @PathVariable Long doctorId,
            @Parameter(description = "التاريخ المطلوب", example = "2024-09-15", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            // @Parameter(description = "الوقت المطلوب", example = "10:30:00", required = true)
            @Parameter(in = ParameterIn.QUERY, name = "time",
                    schema = @Schema(type = "string", description = "الوقت المطلوب" , example = "10:30:00"))
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        try {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new ResourceNotFoundException("الطبيب غير موجود"));

            boolean isAvailable = scheduleService.isDoctorAvailable(doctor, date, time);

            AvailabilityCheckResponse response = new AvailabilityCheckResponse();
            response.setDoctorId(doctorId);
            response.setDate(date);
            response.setTime(time);
            response.setAvailable(isAvailable);
            response.setMessage(isAvailable ? "الطبيب متاح في هذا الوقت" : "الطبيب غير متاح في هذا الوقت");

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم التحقق من التوفر بنجاح", response)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في التحقق من التوفر: " + e.getMessage(), null));
        }
    }

    /**
     * حذف جدولة
     */
    @DeleteMapping("/{scheduleId}")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN')")
    @Operation(
            summary = "🗑️ حذف جدولة",
            description = "حذف جدولة معينة (مدير العيادة فقط)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم حذف الجدولة بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "الجدولة غير موجودة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - مدير العيادة فقط")
    })
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف الجدولة", example = "1")
            @PathVariable Long scheduleId) {
        try {
            scheduleService.deleteSchedule(currentUser.getClinicId(), scheduleId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم حذف الجدولة بنجاح", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في حذف الجدولة: " + e.getMessage(), null));
        }
    }

    /**
     * حذف عدم توفر
     */
    @DeleteMapping("/unavailability/{unavailabilityId}")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR')")
    @Operation(
            summary = "🗑️ حذف عدم توفر",
            description = "حذف فترة عدم توفر معينة"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم حذف فترة عدم التوفر بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "فترة عدم التوفر غير موجودة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<Void>> deleteUnavailability(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف عدم التوفر", example = "1")
            @PathVariable Long unavailabilityId) {
        try {
            scheduleService.deleteUnavailability(currentUser.getClinicId(), unavailabilityId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم حذف فترة عدم التوفر بنجاح", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "فشل في حذف فترة عدم التوفر: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على جداول جميع أطباء العيادة
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "📅 جداول جميع الأطباء",
            description = "الحصول على جداول عمل جميع أطباء العيادة"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على الجداول بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<DoctorScheduleResponse>>> getAllDoctorsSchedules(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading all doctors schedules from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            List<DoctorSchedule> schedules = scheduleService.getAllDoctorsSchedules(
                    effectiveClinicId);

            List<DoctorScheduleResponse> responses = schedules.stream()
                    .map(DoctorScheduleResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على جداول جميع الأطباء بنجاح", responses)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على الجداول: " + e.getMessage(), null));
        }
    }
}