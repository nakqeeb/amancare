// src/main/java/com/nakqeeb/amancare/controller/PublicController.java

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.dto.response.AnnouncementResponse;
import com.nakqeeb.amancare.dto.response.ClinicResponse;
import com.nakqeeb.amancare.dto.response.DoctorAvailabilityResponse;
import com.nakqeeb.amancare.service.PublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "🌐 Public APIs", description = "Public endpoints for landing page and announcements")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicController {

    private final PublicService publicService;

    /**
     * Get active announcements
     */
    @GetMapping("/announcements/active")
    @Operation(
            summary = "📢 Get Active Announcements",
            description = "Get all active announcements for display on landing page"
    )
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getActiveAnnouncements() {
        try {
            List<AnnouncementResponse> announcements = publicService.getActiveAnnouncements();
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على الإعلانات بنجاح", announcements)
            );
        } catch (Exception e) {
            log.error("Error fetching active announcements: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في الحصول على الإعلانات", null));
        }
    }

    /**
     * Get currently available doctors
     */
    @GetMapping("/doctors/available")
    @Operation(
            summary = "👨‍⚕️ Get Available Doctors",
            description = "Get list of doctors currently available for appointments"
    )
    public ResponseEntity<ApiResponse<List<DoctorAvailabilityResponse>>> getAvailableDoctors(
            @Parameter(description = "معرف العيادة (اختياري)")
            @RequestParam(required = false) Long clinicId) {
        try {
            List<DoctorAvailabilityResponse> doctors = publicService.getAvailableDoctors(clinicId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على الأطباء المتاحين بنجاح", doctors)
            );
        } catch (Exception e) {
            log.error("Error fetching available doctors: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في الحصول على الأطباء المتاحين", null));
        }
    }

    /**
     * Get clinic list for public display
     */
    @GetMapping("/clinics")
    @Operation(
            summary = "🏥 Get Public Clinics",
            description = "Get list of active clinics for public booking"
    )
    public ResponseEntity<ApiResponse<List<ClinicResponse>>> getPublicClinics() {
        try {
            List<ClinicResponse> clinics = publicService.getActiveClinics();
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على قائمة العيادات بنجاح", clinics)
            );
        } catch (Exception e) {
            log.error("Error fetching public clinics: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في الحصول على قائمة العيادات", null));
        }
    }
}