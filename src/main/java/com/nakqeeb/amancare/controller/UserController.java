// =============================================================================
// Simple Controller Fix - Avoid Clinic Dependencies for Doctors Endpoint
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.repository.UserRepository;
import com.nakqeeb.amancare.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * وحدة التحكم بالمستخدمين - حل بسيط لمشكلة Lazy Loading
 * Simple UserController - Solves lazy loading issue
 */
@RestController
@RequestMapping("/users")
@Tag(name = "👤 إدارة المستخدمين", description = "APIs الخاصة بالمستخدمين")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * الحصول على جميع الأطباء - حل بسيط بدون تعقيدات
     * Get all doctors - simple solution without complications
     */
    @GetMapping("/doctors")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Transactional(readOnly = true) // Keep session open for this method
    @Operation(
            summary = "👨‍⚕️ قائمة الأطباء",
            description = "الحصول على جميع الأطباء في العيادة"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على قائمة الأطباء بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getDoctors(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {
        try {
            // For READ operations, SYSTEM_ADMIN doesn't need context
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                // SYSTEM_ADMIN can specify clinic or get all
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading all doctors from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                // Other users can only see their clinic
                effectiveClinicId = currentUser.getClinicId();
            }
            List<User> doctors = userRepository.findByClinicIdAndRoleAndIsActiveTrue(
                    effectiveClinicId,
                    UserRole.DOCTOR
            );

            List<DoctorResponse> doctorResponses = doctors.stream()
                    .map(DoctorResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على قائمة الأطباء بنجاح", doctorResponses)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على قائمة الأطباء: " + e.getMessage(), null));
        }
    }

    /**
     * استجابة مبسطة للأطباء - بدون تعقيدات Lazy Loading
     * Simple doctor response - no lazy loading complications
     */
    public static class DoctorResponse {
        public Long id;
        public String fullName;
        public String specialization;
        public String phone;
        public String email;

        public static DoctorResponse fromEntity(User user) {
            DoctorResponse response = new DoctorResponse();
            response.id = user.getId();
            response.fullName = user.getFirstName() + " " + user.getLastName();
            response.specialization = user.getSpecialization();
            response.phone = user.getPhone();
            response.email = user.getEmail();
            return response;
        }
    }
}