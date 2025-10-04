package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.response.ActivityLogResponse;
import com.nakqeeb.amancare.dto.response.ActivityStatisticsResponse;
import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.entity.ActionType;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.ActivityLogService;
import com.nakqeeb.amancare.service.ClinicContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * وحدة التحكم بسجلات الأنشطة
 * Activity Log Controller - Only accessible by ADMIN and SYSTEM_ADMIN
 */
@RestController
@RequestMapping("/admin/activities")
@Tag(name = "📊 سجلات الأنشطة", description = "APIs الخاصة بسجلات أنشطة العيادة")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActivityLogController {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogController.class);

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ClinicContextService clinicContextService;

    // =============================================================================
    // GET RECENT ACTIVITIES
    // =============================================================================

    /**
     * Get recent activities for clinic
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(
            summary = "🕐 الأنشطة الأخيرة",
            description = """
            الحصول على الأنشطة الأخيرة في العيادة
            - ADMIN: يرى أنشطة عيادته فقط
            - SYSTEM_ADMIN: يرى أنشطة العيادة المحددة في السياق
            """
    )
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getRecentActivities(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "عدد الأنشطة (افتراضي 50)")
            @RequestParam(defaultValue = "50") int limit
    ) {
        try {
            // Get clinic ID based on user role
            Long clinicId = getEffectiveClinicId(currentUser);

            if (clinicId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "لم يتم تحديد سياق العيادة", null));
            }

            List<ActivityLogResponse> activities = activityLogService.getRecentActivities(clinicId, limit);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب الأنشطة بنجاح", activities)
            );

        } catch (Exception e) {
            logger.error("Error fetching recent activities: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "خطأ في جلب الأنشطة: " + e.getMessage(), null));
        }
    }

    // =============================================================================
    // SEARCH ACTIVITIES WITH FILTERS
    // =============================================================================

    /**
     * Search activities with filters
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(
            summary = "🔍 البحث في الأنشطة",
            description = """
            البحث في سجلات الأنشطة مع إمكانية التصفية:
            - حسب المستخدم
            - حسب نوع الإجراء (CREATE, UPDATE, DELETE)
            - حسب نوع الكيان (Patient, Appointment, etc.)
            - حسب النطاق الزمني
            - بحث نصي
            """
    )
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> searchActivities(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف المستخدم")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "نوع الإجراء")
            @RequestParam(required = false) ActionType actionType,
            @Parameter(description = "نوع الكيان")
            @RequestParam(required = false) String entityType,
            @Parameter(description = "تاريخ البداية")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "تاريخ النهاية")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "مصطلح البحث")
            @RequestParam(required = false) String searchTerm,
            @Parameter(description = "رقم الصفحة")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "ترتيب حسب")
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @Parameter(description = "اتجاه الترتيب")
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            // Get clinic ID based on user role
            Long clinicId = getEffectiveClinicId(currentUser);

            if (clinicId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "لم يتم تحديد سياق العيادة", null));
            }

            // Create pageable
            Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // Search activities
            Page<ActivityLogResponse> activities = activityLogService.searchActivities(
                    clinicId, userId, actionType, entityType,
                    startDate, endDate, searchTerm, pageable
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم البحث بنجاح", activities)
            );

        } catch (Exception e) {
            logger.error("Error searching activities: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "خطأ في البحث: " + e.getMessage(), null));
        }
    }

    // =============================================================================
    // GET ENTITY ACTIVITY TRAIL
    // =============================================================================

    /**
     * Get activity trail for a specific entity
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(
            summary = "📜 سجل نشاط الكيان",
            description = "الحصول على سجل جميع الأنشطة المتعلقة بكيان معين (مريض، موعد، إلخ)"
    )
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getEntityActivityTrail(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "نوع الكيان")
            @PathVariable String entityType,
            @Parameter(description = "معرف الكيان")
            @PathVariable Long entityId
    ) {
        try {
            // Get clinic ID based on user role
            Long clinicId = getEffectiveClinicId(currentUser);

            if (clinicId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "لم يتم تحديد سياق العيادة", null));
            }

            List<ActivityLogResponse> activities = activityLogService.getEntityActivityTrail(
                    clinicId, entityType, entityId
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب سجل النشاط بنجاح", activities)
            );

        } catch (Exception e) {
            logger.error("Error fetching entity activity trail: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "خطأ في جلب سجل النشاط: " + e.getMessage(), null));
        }
    }

    // =============================================================================
    // GET ACTIVITIES BY USER
    // =============================================================================

    /**
     * Get activities by user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(
            summary = "👤 أنشطة المستخدم",
            description = "الحصول على جميع أنشطة مستخدم معين"
    )
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> getActivitiesByUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف المستخدم")
            @PathVariable Long userId,
            @Parameter(description = "رقم الصفحة")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة")
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            // Get clinic ID based on user role
            Long clinicId = getEffectiveClinicId(currentUser);

            if (clinicId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "لم يتم تحديد سياق العيادة", null));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<ActivityLogResponse> activities = activityLogService.getActivitiesByUser(
                    clinicId, userId, pageable
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب أنشطة المستخدم بنجاح", activities)
            );

        } catch (Exception e) {
            logger.error("Error fetching user activities: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "خطأ في جلب أنشطة المستخدم: " + e.getMessage(), null));
        }
    }

    // =============================================================================
    // GET STATISTICS
    // =============================================================================

    /**
     * Get activity statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(
            summary = "📈 إحصائيات الأنشطة",
            description = "الحصول على إحصائيات شاملة حول أنشطة العيادة"
    )
    public ResponseEntity<ApiResponse<ActivityStatisticsResponse>> getStatistics(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "منذ (افتراضي: آخر 30 يوم)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since
    ) {
        try {
            // Get clinic ID based on user role
            Long clinicId = getEffectiveClinicId(currentUser);

            if (clinicId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "لم يتم تحديد سياق العيادة", null));
            }

            // Default to last 30 days if not specified
            if (since == null) {
                since = LocalDateTime.now().minusDays(30);
            }

            ActivityStatisticsResponse stats = activityLogService.getActivityStatistics(clinicId, since);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب الإحصائيات بنجاح", stats)
            );

        } catch (Exception e) {
            logger.error("Error fetching activity statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "خطأ في جلب الإحصائيات: " + e.getMessage(), null));
        }
    }

    // =============================================================================
    // HELPER METHODS
    // =============================================================================

    /**
     * Get effective clinic ID based on user role
     */
    private Long getEffectiveClinicId(UserPrincipal currentUser) {
        if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            // SYSTEM_ADMIN must have clinic context set
            return clinicContextService.getEffectiveClinicId(currentUser);
        } else {
            // Regular ADMIN uses their own clinic
            return currentUser.getClinicId();
        }
    }
}