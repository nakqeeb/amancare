// src/main/java/com/nakqeeb/amancare/controller/GuestBookingController.java

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.request.GuestBookingRequest;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.entity.DoctorSchedule;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.GuestBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/guest")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "🎫 حجز المواعيد للضيوف", description = "APIs لحجز المواعيد بدون حساب")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GuestBookingController {

    private final GuestBookingService guestBookingService;

    // ============================================================================
    // GET ALL ACTIVE CLINICS
    // ============================================================================

    @GetMapping("/clinics")
    @Operation(
            summary = "📋 جلب جميع العيادات النشطة",
            description = "إرجاع قائمة بجميع العيادات النشطة المتاحة للحجز من قِبل الزوار (Guest Booking)"
    )
    public ResponseEntity<ApiResponse<List<ClinicSummaryResponse>>> getAllActiveClinics() {
        log.info("REST request to get all active clinics for guest booking");

        List<ClinicSummaryResponse> clinics = guestBookingService.getAllActiveClinics();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "تم جلب قائمة العيادات بنجاح", clinics));
    }

    /**
     * Get all doctors for a clinic
     */
    @GetMapping("/clinics/{clinicId}/doctors")
    @Operation(
            summary = "👨‍⚕️ عرض الأطباء",
            description = "عرض قائمة الأطباء المتاحين في العيادة مع أيام عملهم"
    )
    public ResponseEntity<ApiResponse<List<ClinicDoctorSummary>>> getClinicDoctors(
            @Parameter(description = "معرف العيادة")
            @PathVariable Long clinicId) {
        try {
            List<ClinicDoctorSummary> doctors = guestBookingService.getClinicDoctors(clinicId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على قائمة الأطباء بنجاح", doctors)
            );
        } catch (Exception e) {
            log.error("Error fetching clinic doctors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * الحصول على جدولة طبيب
     */
    @GetMapping("/doctor/{doctorId}/schedules")
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
            @Parameter(description = "معرف العيادة")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "معرف الطبيب")
            @PathVariable Long doctorId) {
        try {
            List<DoctorSchedule> schedules = guestBookingService.getDoctorSchedule(
                    clinicId, doctorId);

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
     * Get available time slots for a doctor
     */
    @GetMapping("/clinics/{clinicId}/doctors/{doctorId}/available-slots")
    @Operation(
            summary = "🕐 الأوقات المتاحة",
            description = "عرض الأوقات المتاحة للطبيب في تاريخ معين"
    )
    public ResponseEntity<ApiResponse<List<java.time.LocalTime>>> getAvailableTimeSlots(
            @Parameter(description = "معرف العيادة", example = "1")
            @PathVariable Long clinicId,
            @Parameter(description = "معرف الطبيب", example = "2")
            @PathVariable Long doctorId,
            @Parameter(description = "التاريخ المطلوب", example = "2024-10-15", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "مدة الموعد بالدقائق", example = "30")
            @RequestParam(defaultValue = "30") Integer durationMinutes) {
        try {
            List<java.time.LocalTime> slots = guestBookingService.getAvailableTimeSlots(
                    clinicId, doctorId, date, durationMinutes);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على الأوقات المتاحة بنجاح", slots)
            );
        } catch (Exception e) {
            log.error("Error fetching available slots: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Book appointment as guest
     */
    @PostMapping("/book-appointment")
    @Operation(
            summary = "📝 حجز موعد",
            description = "حجز موعد جديد بدون الحاجة لحساب. سيتم إرسال رقم المريض عبر البريد الإلكتروني"
    )
    public ResponseEntity<ApiResponse<GuestBookingResponse>> bookAppointment(
            @Valid @RequestBody GuestBookingRequest request) {
        try {
            GuestBookingResponse response = guestBookingService.bookAppointmentAsGuest(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "تم حجز الموعد بنجاح", response));
        } catch (Exception e) {
            log.error("Error booking appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Confirm appointment via email link
     */
    @PostMapping("/confirm-appointment")
    @Operation(
            summary = "✅ تأكيد الموعد",
            description = "تأكيد الموعد باستخدام الرابط المرسل عبر البريد الإلكتروني"
    )
    public ResponseEntity<ApiResponse<Void>> confirmAppointment(
            @Parameter(description = "رمز التأكيد", required = true)
            @RequestParam String token) {
        try {
            guestBookingService.confirmAppointment(token);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تأكيد موعدك بنجاح", null)
            );
        } catch (Exception e) {
            log.error("Error confirming appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Get patient appointments by patient number
     */
    @GetMapping("/appointments")
    @Operation(
            summary = "📋 عرض مواعيدي",
            description = "عرض جميع المواعيد المستقبلية باستخدام رقم المريض"
    )
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMyAppointments(
            @Parameter(description = "رقم المريض", required = true)
            @RequestParam String patientNumber) {
        try {
            List<AppointmentResponse> appointments =
                    guestBookingService.getPatientAppointments(patientNumber);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على المواعيد بنجاح", appointments)
            );
        } catch (Exception e) {
            log.error("Error fetching appointments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Cancel appointment
     */
    @DeleteMapping("/appointments/{appointmentId}")
    @Operation(
            summary = "❌ إلغاء الموعد",
            description = "إلغاء موعد باستخدام رقم المريض"
    )
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(
            @Parameter(description = "معرف الموعد", example = "1")
            @PathVariable Long appointmentId,
            @Parameter(description = "رقم المريض", required = true)
            @RequestParam String patientNumber) {
        try {
            guestBookingService.cancelAppointmentByPatient(appointmentId, patientNumber);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم إلغاء الموعد بنجاح", null)
            );
        } catch (Exception e) {
            log.error("Error cancelling appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}