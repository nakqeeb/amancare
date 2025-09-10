
// =============================================================================
// Audit Controller - REST endpoints for audit log access
// src/main/java/com/nakqeeb/amancare/controller/AuditController.java
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.dto.response.AuditLogResponse;
import com.nakqeeb.amancare.dto.response.AuditStatisticsResponse;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for audit log management
 * Only accessible by SYSTEM_ADMIN role
 */
@RestController
@RequestMapping("/admin/audit")
@Tag(name = "🔍 سجلات المراجعة", description = "Audit logs and system monitoring")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuditController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Get audit logs with filtering and pagination
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "📋 سجلات المراجعة",
            description = """
            الحصول على سجلات المراجعة مع إمكانية التصفية:
            - حسب المستخدم المسؤول
            - حسب العيادة
            - حسب نوع الإجراء
            - حسب نطاق زمني
            """
    )
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditLogs(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف المستخدم المسؤول")
            @RequestParam(required = false) Long adminUserId,
            @Parameter(description = "معرف العيادة")
            @RequestParam(required = false) Long clinicId,
            @Parameter(description = "نوع الإجراء")
            @RequestParam(required = false) String actionType,
            @Parameter(description = "نوع المورد")
            @RequestParam(required = false) String resourceType,
            @Parameter(description = "تاريخ البداية")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "تاريخ النهاية")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "رقم الصفحة")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "ترتيب حسب")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "اتجاه الترتيب")
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        // Only SYSTEM_ADMIN can access audit logs
        if (!UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "غير مصرح بالوصول لسجلات المراجعة", null));
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuditLogResponse> logs = auditLogService.getAuditLogs(
                adminUserId, clinicId, actionType, resourceType,
                startDate, endDate, pageable
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "سجلات المراجعة", logs)
        );
    }

    /**
     * Get recent actions for current SYSTEM_ADMIN
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "🕐 الإجراءات الأخيرة",
            description = "الحصول على آخر الإجراءات للمستخدم الحالي"
    )
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getRecentActions(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "عدد السجلات")
            @RequestParam(defaultValue = "10") int limit) {

        List<AuditLogResponse> recentActions = auditLogService.getRecentActions(
                currentUser.getId(), limit
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "الإجراءات الأخيرة", recentActions)
        );
    }

    /**
     * Get audit statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "📊 إحصائيات المراجعة",
            description = "الحصول على إحصائيات شاملة لسجلات المراجعة"
    )
    public ResponseEntity<ApiResponse<AuditStatisticsResponse>> getAuditStatistics(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "معرف المستخدم المسؤول")
            @RequestParam(required = false) Long adminUserId,
            @Parameter(description = "تاريخ البداية")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "تاريخ النهاية")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        AuditStatisticsResponse statistics = auditLogService.getAuditStatistics(
                adminUserId, startDate, endDate
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "إحصائيات المراجعة", statistics)
        );
    }

    /**
     * Get audit trail for specific resource
     */
    @GetMapping("/resource/{resourceType}/{resourceId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "📄 سجل المورد",
            description = "الحصول على سجل المراجعة الكامل لمورد محدد"
    )
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getResourceAuditTrail(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String resourceType,
            @PathVariable Long resourceId) {

        List<AuditLogResponse> auditTrail = auditLogService.getResourceAuditTrail(
                resourceType.toUpperCase(), resourceId
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "سجل مراجعة المورد", auditTrail)
        );
    }

    /**
     * Export audit logs to CSV
     */
    @GetMapping("/export/csv")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "📥 تصدير السجلات",
            description = "تصدير سجلات المراجعة إلى ملف CSV"
    )
    public ResponseEntity<String> exportAuditLogs(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "تاريخ البداية", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @Parameter(description = "تاريخ النهاية", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {

        String csvContent = auditLogService.exportAuditLogsToCsv(startDate, endDate);

        // Log the export action itself
        auditLogService.logAction(
                currentUser.getId(),
                "EXPORT",
                null,
                "AUDIT_LOGS",
                null,
                String.format("Exported audit logs from %s to %s", startDate, endDate)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment",
                String.format("audit_logs_%s_%s.csv",
                        startDate.toLocalDate(), endDate.toLocalDate()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
    }

    /**
     * Check if current user has recent activity
     */
    @GetMapping("/activity/check")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "✅ فحص النشاط",
            description = "التحقق من وجود نشاط حديث للمستخدم"
    )
    public ResponseEntity<ApiResponse<Boolean>> checkRecentActivity(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "نطاق الوقت بالدقائق")
            @RequestParam(defaultValue = "30") int withinMinutes) {

        boolean hasActivity = auditLogService.hasRecentActivity(
                currentUser.getId(), withinMinutes
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true,
                        hasActivity ? "يوجد نشاط حديث" : "لا يوجد نشاط حديث",
                        hasActivity)
        );
    }
}