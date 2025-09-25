// =============================================================================
// Refactored UserController - Uses Service Layer Only
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.annotation.SystemAdminContext;
import com.nakqeeb.amancare.dto.request.UpdateUserRequest;
import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.dto.response.ClinicUserResponse;
import com.nakqeeb.amancare.dto.response.ClinicUserStats;
import com.nakqeeb.amancare.dto.response.UserResponse;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.ClinicContextService;
import com.nakqeeb.amancare.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private UserService userService;

    @Autowired
    private ClinicContextService clinicContextService;

    /**
     * الحصول على جميع الأطباء - حل بسيط بدون تعقيدات
     * Get all doctors - simple solution without complications
     */
    @GetMapping("/doctors")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
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
            // Determine clinic ID based on user role
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading doctors from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                effectiveClinicId = currentUser.getClinicId();
            }

            // Get doctors through service
            List<User> doctors = userService.getDoctorsByClinic(effectiveClinicId);

            List<DoctorResponse> doctorResponses = doctors.stream()
                    .map(DoctorResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على قائمة الأطباء بنجاح", doctorResponses)
            );
        } catch (Exception e) {
            logger.error("Error fetching doctors: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على قائمة الأطباء: " + e.getMessage(), null));
        }
    }

    /**
     * الحصول على جميع المستخدمين في العيادة مع إمكانية الفلترة حسب الدور
     * Get all clinic users with optional role filtering - For ADMIN only
     */
    @GetMapping("/clinic-users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN')")
    @Operation(
            summary = "👥 قائمة مستخدمي العيادة",
            description = "الحصول على جميع المستخدمين في العيادة (أطباء، ممرضين، موظفي استقبال) مع إمكانية الفلترة حسب الدور"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على قائمة المستخدمين بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح - يجب تسجيل الدخول"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - صلاحيات غير كافية"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "معاملات غير صحيحة")
    })
    public ResponseEntity<ApiResponse<List<ClinicUserResponse>>> getClinicUsers(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "فلترة حسب الدور (DOCTOR, NURSE, RECEPTIONIST)")
            @RequestParam(required = false) String role,
            @Parameter(description = "عرض المستخدمين النشطين فقط", example = "true")
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {

        try {
            logger.info("Controller: Getting clinic users for admin {}, role: {}, activeOnly: {}",
                    currentUser.getUsername(), role, activeOnly);

            // Determine clinic ID based on user role
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading users from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                effectiveClinicId = currentUser.getClinicId();
            }

            // Call service method
            List<ClinicUserResponse> users = userService.getClinicUsers(effectiveClinicId, role, activeOnly);

            return ResponseEntity.ok(new ApiResponse<>(true,
                    "تم الحصول على قائمة المستخدمين بنجاح",
                    users));

        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument: ", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error fetching clinic users: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "حدث خطأ في الحصول على قائمة المستخدمين", null));
        }
    }

    /**
     * الحصول على إحصائيات المستخدمين في العيادة
     * Get clinic users statistics
     */
    @GetMapping("/clinic-users/stats")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN')")
    @Operation(
            summary = "📊 إحصائيات مستخدمي العيادة",
            description = "الحصول على إحصائيات المستخدمين في العيادة حسب الأدوار"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "تم الحصول على الإحصائيات بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "غير مصرح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع")
    })
    public ResponseEntity<ApiResponse<ClinicUserStats>> getClinicUserStats(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId) {

        try {
            logger.info("Controller: Getting clinic stats for admin {}", currentUser.getUsername());

            // Determine clinic ID based on user role
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading stats from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                effectiveClinicId = currentUser.getClinicId();
            }

            // Call service method
            ClinicUserStats stats = userService.getClinicUserStats(effectiveClinicId);

            return ResponseEntity.ok(new ApiResponse<>(true,
                    "تم الحصول على الإحصائيات بنجاح",
                    stats));

        } catch (Exception e) {
            logger.error("Error fetching clinic user stats: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "حدث خطأ في الحصول على الإحصائيات", null));
        }
    }

    /**
     * Get specific clinic user by ID
     */
    @GetMapping("/clinic-users/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN')")
    @Operation(
            summary = "👤 الحصول على معلومات مستخدم محدد",
            description = "الحصول على معلومات مستخدم محدد في العيادة"
    )
    public ResponseEntity<ApiResponse<ClinicUserResponse>> getClinicUserById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف العيادة (للـ SYSTEM_ADMIN فقط)")
            @RequestParam(required = false) Long clinicId,
            @PathVariable Long userId) {

        try {
            // Determine clinic ID based on user role
            Long effectiveClinicId;
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                effectiveClinicId = clinicId; // Can be null to get all clinics
                logger.info("SYSTEM_ADMIN reading clinic user by ID from clinic: {}",
                        clinicId != null ? clinicId : "ALL");
            } else {
                effectiveClinicId = currentUser.getClinicId();
            }
            ClinicUserResponse user = userService.getClinicUserById(effectiveClinicId, userId);
            return ResponseEntity.ok(new ApiResponse<>(true,
                    "تم الحصول على معلومات المستخدم بنجاح",
                    user));
        } catch (Exception e) {
            logger.error("Error fetching user {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Toggle user active status
     */
    @PutMapping("/clinic-users/{userId}/toggle-status")
    @SystemAdminContext
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN')")
    @Operation(
            summary = "🔄 تغيير حالة المستخدم",
            description = "تفعيل أو تعطيل مستخدم في العيادة"
    )
    public ResponseEntity<ApiResponse<ClinicUserResponse>> toggleUserStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long userId,
            @RequestParam boolean isActive) {

        try {
            // Log if SYSTEM_ADMIN is acting with context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                ClinicContextService.ClinicContextInfo contextInfo =
                        clinicContextService.getCurrentContext(currentUser);
                logger.info("SYSTEM_ADMIN is updating a patient with clinic context. ActingClinicId: {}, Reason: {}",
                        contextInfo.getActingAsClinicId(), contextInfo.getReason());
            }

            ClinicUserResponse user = userService.toggleClinicUserStatus(currentUser, userId, isActive);
            String message = isActive ? "تم تفعيل المستخدم بنجاح" : "تم تعطيل المستخدم بنجاح";
            return ResponseEntity.ok(new ApiResponse<>(true, message, user));
        } catch (Exception e) {
            logger.error("Error toggling user status {}: ", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * الحصول على جميع العيادات (من خلال مستخدمي ADMIN)
     * Get all clinics (via ADMIN users)
     */
    @GetMapping("/clinics")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "🏥 قائمة العيادات",
            description = "الحصول على جميع العيادات من خلال مستخدمي ADMIN (بواسطة SYSTEM_ADMIN)"
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
     * تحديث بيانات المستخدم
     * Update User API
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(
            summary = "تحديث بيانات المستخدم",
            description = """
            تحديث بيانات المستخدم في النظام مع مراعاة الصلاحيات:
            
            **قواعد الصلاحيات:**
            - **مدير النظام (SYSTEM_ADMIN)**: يمكنه تحديث أي مستخدم من أي دور
            - **مدير العيادة (ADMIN)**: يمكنه تحديث نفسه أو مستخدمين من عيادته فقط
            - **الأدوار الأخرى**: يمكنهم تحديث بياناتهم الشخصية فقط
            
            **البيانات القابلة للتحديث:**
            - البيانات الشخصية (الاسم، البريد الإلكتروني، الهاتف)
            - الدور والتخصص
            - حالة التفعيل
            - كلمة المرور (اختيارية)
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "تم تحديث المستخدم بنجاح",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "بيانات غير صحيحة أو البريد الإلكتروني مستخدم من قبل",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "غير مسموح - يجب تسجيل الدخول",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "ممنوع - ليس لديك صلاحية لتحديث هذا المستخدم",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "المستخدم غير موجود",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "معرف المستخدم", required = true, example = "1")
            @PathVariable Long id,

            @Parameter(description = "بيانات تحديث المستخدم", required = true)
            @Valid @RequestBody UpdateUserRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal currentUser) {

        logger.info("طلب تحديث المستخدم - المعرف: {}, المستخدم الحالي: {}", id, currentUser.getUsername());

            UserResponse updatedUser = userService.updateUser(id, request, currentUser);

            ApiResponse<UserResponse> response = new ApiResponse<>();
            response.setSuccess(true);
            response.setMessage("تم تحديث بيانات المستخدم بنجاح");
            response.setData(updatedUser);

            logger.info("تم تحديث المستخدم بنجاح - المعرف: {}", id);
            return ResponseEntity.ok(response);
    }


    // =================== Response DTOs (Keep these for backward compatibility) ===================

    /**
     * Simple doctor response DTO
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

    /**
     * Clinic response DTO
     */
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

    /**
     * Admin user response DTO
     */
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

    /**
     * طلب تحديث كلمة المرور
     */
    public static class PasswordUpdateRequest {

        @Schema(description = "كلمة المرور الجديدة", required = true, minLength = 6)
        @Size(min = 6, message = "كلمة المرور يجب أن تكون على الأقل 6 أحرف")
        @NotBlank(message = "كلمة المرور الجديدة مطلوبة")
        private String newPassword;

        public PasswordUpdateRequest() {}

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}