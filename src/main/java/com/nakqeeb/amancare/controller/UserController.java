// =============================================================================
// Simple Controller Fix - Avoid Clinic Dependencies for Doctors Endpoint
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.repository.UserRepository;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.UserService;
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
import java.util.stream.Collectors;

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

    @Autowired
    private UserService userService;

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
     * الحصول على جميع العيادات (من خلال مستخدمي ADMIN)
     * Get all clinics (via ADMIN users)
     */
    @GetMapping("/clinics")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Transactional(readOnly = true)
    @Operation(
            summary = "🏥 قائمة العيادات",
            description = "(الحصول على جميع العيادات من خلال مستخدمي ADMIN) (بواسطة SYSTEM_ADMIN)"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على قائمة العيادات بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية")
    })
    public ResponseEntity<ApiResponse<List<ClinicResponse>>> getAllClinics() {
        try {
            List<Clinic> clinics = userService.getAllClinicsFromAdmins();
            List<ClinicResponse> clinicResponses = clinics.stream()
                    .map(ClinicResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على قائمة العيادات بنجاح", clinicResponses)
            );
        } catch (Exception e) {
            logger.error("Failed to get clinics list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على قائمة العيادات: " + e.getMessage(), null));
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

    public static class ClinicResponse {
        public Long id;
        public String name;
        public String description;
        public String address;
        public String phone;
        public String email;
        public Boolean isActive;
        public String subscriptionPlan;

        public static ClinicResponse fromEntity(Clinic clinic) {
            ClinicResponse response = new ClinicResponse();
            response.id = clinic.getId();
            response.name = clinic.getName();
            response.description = clinic.getDescription();
            response.address = clinic.getAddress();
            response.phone = clinic.getPhone();
            response.email = clinic.getEmail();
            response.isActive = clinic.getIsActive();
            response.subscriptionPlan = clinic.getSubscriptionPlan() != null ?
                    clinic.getSubscriptionPlan().name() : null;

            return response;
        }
    }

    public static class AdminUserResponse {
        public Long id;
        public String username;
        public String email;
        public String fullName;
        public String phone;
        public UserRole role;
        public Boolean isActive;
        public ClinicResponse clinic;

        public static AdminUserResponse fromEntity(User user) {
            AdminUserResponse response = new AdminUserResponse();
            response.id = user.getId();
            response.username = user.getUsername();
            response.email = user.getEmail();
            response.fullName = user.getFirstName() + " " + user.getLastName();
            response.phone = user.getPhone();
            response.role = user.getRole();
            response.isActive = user.getIsActive();

            if (user.getClinic() != null) {
                response.clinic = ClinicResponse.fromEntity(user.getClinic());
            }

            return response;
        }
    }
}