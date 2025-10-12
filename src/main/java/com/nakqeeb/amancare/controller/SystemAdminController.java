// =============================================================================
// System Admin Controller - وحدة التحكم بإدارة النظام
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.request.CreateAnnouncementRequest;
import com.nakqeeb.amancare.dto.request.UpdateAnnouncementRequest;
import com.nakqeeb.amancare.dto.response.AnnouncementResponse;
import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/announcements")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "🔐 الإدارة النظامية - الإعلانات",
        description = "إدارة الإعلانات بواسطة مدير النظام (SYSTEM_ADMIN)"
)
@CrossOrigin(origins = "*", maxAge = 3600)
public class SystemAdminController {

    private final AnnouncementService announcementService;

    /**
     * Get all announcements with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "📋 عرض جميع الإعلانات (بشكل مجزأ)",
            description = "جلب جميع الإعلانات مع دعم التقسيم إلى صفحات لأغراض الإدارة"
    )
    public ResponseEntity<ApiResponse<Page<AnnouncementResponse>>> getAllAnnouncements(
            @Parameter(description = "رقم الصفحة (يبدأ من 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "عدد العناصر في الصفحة")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ترتيب حسب")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "اتجاه الترتيب")
            @RequestParam(defaultValue = "DESC") String direction) {
        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC")
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

            Page<AnnouncementResponse> announcements = announcementService.getAllAnnouncements(pageable);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على الإعلانات بنجاح", announcements)
            );
        } catch (Exception e) {
            log.error("Error fetching announcements: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في الحصول على الإعلانات: " + e.getMessage(), null));
        }
    }

    /**
     * Get all announcements as list (no pagination)
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "📋 عرض جميع الإعلانات (قائمة كاملة)",
            description = "جلب جميع الإعلانات كقائمة بسيطة بدون تقسيم إلى صفحات"
    )
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getAllAnnouncementsList() {
        try {
            List<AnnouncementResponse> announcements = announcementService.getAllAnnouncementsList();
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على الإعلانات بنجاح", announcements)
            );
        } catch (Exception e) {
            log.error("Error fetching announcements list: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في الحصول على الإعلانات: " + e.getMessage(), null));
        }
    }

    /**
     * Get announcement by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "🔍 عرض الإعلان حسب المعرف",
            description = "جلب تفاصيل إعلان محدد باستخدام المعرف (ID)"
    )
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getAnnouncementById(
            @Parameter(description = "معرف الإعلان") @PathVariable Long id) {
        try {
            AnnouncementResponse announcement = announcementService.getAnnouncementById(id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على الإعلان بنجاح", announcement)
            );
        } catch (Exception e) {
            log.error("Error fetching announcement {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في الحصول على الإعلان: " + e.getMessage(), null));
        }
    }

    /**
     * Create new announcement
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "➕ إنشاء إعلان جديد",
            description = "إضافة إعلان جديد إلى النظام"
    )
    public ResponseEntity<ApiResponse<AnnouncementResponse>> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request) {
        try {
            AnnouncementResponse announcement = announcementService.createAnnouncement(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "تم إنشاء الإعلان بنجاح", announcement));
        } catch (Exception e) {
            log.error("Error creating announcement: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في إنشاء الإعلان: " + e.getMessage(), null));
        }
    }

    /**
     * Update announcement
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "✏️ تحديث الإعلان",
            description = "تعديل إعلان موجود في النظام"
    )
    public ResponseEntity<ApiResponse<AnnouncementResponse>> updateAnnouncement(
            @Parameter(description = "معرف الإعلان") @PathVariable Long id,
            @Valid @RequestBody UpdateAnnouncementRequest request) {
        try {
            AnnouncementResponse announcement = announcementService.updateAnnouncement(id, request);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تحديث الإعلان بنجاح", announcement)
            );
        } catch (Exception e) {
            log.error("Error updating announcement {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في تحديث الإعلان: " + e.getMessage(), null));
        }
    }

    /**
     * Activate announcement
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "✅ تفعيل الإعلان",
            description = "تفعيل الإعلان ليصبح مرئيًا"
    )
    public ResponseEntity<ApiResponse<AnnouncementResponse>> activateAnnouncement(
            @Parameter(description = "معرف الإعلان") @PathVariable Long id) {
        try {
            AnnouncementResponse announcement = announcementService.activateAnnouncement(id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تفعيل الإعلان بنجاح", announcement)
            );
        } catch (Exception e) {
            log.error("Error activating announcement {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في تفعيل الإعلان: " + e.getMessage(), null));
        }
    }

    /**
     * Deactivate announcement
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "❌ إلغاء تفعيل الإعلان",
            description = "إلغاء تفعيل الإعلان لإخفائه"
    )
    public ResponseEntity<ApiResponse<AnnouncementResponse>> deactivateAnnouncement(
            @Parameter(description = "معرف الإعلان") @PathVariable Long id) {
        try {
            AnnouncementResponse announcement = announcementService.deactivateAnnouncement(id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم إلغاء تفعيل الإعلان بنجاح", announcement)
            );
        } catch (Exception e) {
            log.error("Error deactivating announcement {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في إلغاء تفعيل الإعلان: " + e.getMessage(), null));
        }
    }

    /**
     * Delete announcement
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "🗑️ حذف الإعلان",
            description = "حذف الإعلان بشكل دائم"
    )
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(
            @Parameter(description = "معرف الإعلان") @PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم حذف الإعلان بنجاح", null)
            );
        } catch (Exception e) {
            log.error("Error deleting announcement {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "فشل في حذف الإعلان: " + e.getMessage(), null));
        }
    }
}