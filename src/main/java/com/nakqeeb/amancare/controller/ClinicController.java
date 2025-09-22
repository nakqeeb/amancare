// =============================================================================
// Clinic Controller - وحدة التحكم بالعيادات
// src/main/java/com/nakqeeb/amancare/controller/ClinicController.java
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.annotation.SystemAdminContext;
import com.nakqeeb.amancare.dto.request.CreateClinicRequest;
import com.nakqeeb.amancare.dto.request.UpdateClinicRequest;
import com.nakqeeb.amancare.dto.request.UpdateSubscriptionRequest;
import com.nakqeeb.amancare.dto.response.*;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.ClinicService;
import com.nakqeeb.amancare.service.ClinicContextService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * وحدة التحكم بالعيادات
 * REST Controller for clinic management operations
 */
@RestController
@RequestMapping("/clinics")
@Tag(name = "🏥 إدارة العيادات", description = "APIs الخاصة بإدارة العيادات")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClinicController {
    private static final Logger logger = LoggerFactory.getLogger(ClinicController.class);

    @Autowired
    private ClinicService clinicService;

    @Autowired
    private ClinicContextService clinicContextService;

    // ===================================================================
    // CREATE OPERATIONS
    // ===================================================================

    /**
     * Create a new clinic (SYSTEM_ADMIN only)
     */
    /* @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "➕ إنشاء عيادة جديدة",
            description = """
            إنشاء عيادة جديدة في النظام (SYSTEM_ADMIN فقط)
            - يتم تعيين خطة الاشتراك الأساسية افتراضياً
            - العيادة تكون نشطة مباشرة بعد الإنشاء
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "مثال على إنشاء عيادة",
                                    value = """
                                    {
                                      "name": "عيادة الأمان الطبية",
                                      "description": "عيادة متخصصة في طب الأسرة",
                                      "address": "شارع الملك فهد، الرياض",
                                      "phone": "+966501234567",
                                      "email": "info@aman-clinic.com",
                                      "workingHoursStart": "08:00",
                                      "workingHoursEnd": "20:00",
                                      "workingDays": "SUN,MON,TUE,WED,THU",
                                      "subscriptionPlan": "PREMIUM"
                                    }
                                    """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "تم إنشاء العيادة بنجاح"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "بيانات غير صحيحة"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "غير مصرح - SYSTEM_ADMIN فقط"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "العيادة موجودة مسبقاً بنفس الاسم"
            )
    })
    public ResponseEntity<ApiResponse<ClinicResponse>> createClinic(
            @Valid @RequestBody CreateClinicRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Creating new clinic: {} by SYSTEM_ADMIN: {}",
                    request.getName(), currentUser.getId());

            ClinicResponse clinic = clinicService.createClinic(request, currentUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(true, "تم إنشاء العيادة بنجاح", clinic)
            );
        } catch (Exception e) {
            logger.error("Error creating clinic: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل إنشاء العيادة: " + e.getMessage(), null)
            );
        }
    } */

    // ===================================================================
    // READ OPERATIONS
    // ===================================================================

    /**
     * Get all clinics with pagination (SYSTEM_ADMIN only)
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "📋 قائمة العيادات",
            description = "الحصول على قائمة جميع العيادات مع إمكانية البحث والترقيم (SYSTEM_ADMIN فقط)"
    )
    public ResponseEntity<ApiResponse<Page<ClinicResponse>>> getAllClinics(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(description = "البحث بالاسم")
            @RequestParam(required = false) String search,
            @Parameter(description = "تصفية حسب الحالة (نشط/غير نشط)")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "رقم الصفحة", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "حجم الصفحة", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "ترتيب حسب", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "اتجاه الترتيب", example = "ASC")
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<ClinicResponse> clinics = clinicService.getAllClinics(
                    pageable, search, isActive, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب قائمة العيادات بنجاح", clinics)
            );
        } catch (Exception e) {
            logger.error("Error fetching clinics: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل جلب قائمة العيادات: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Get clinic by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN')")
    @Operation(
            summary = "🔍 تفاصيل العيادة",
            description = """
            الحصول على تفاصيل عيادة محددة
            - SYSTEM_ADMIN: يمكنه عرض أي عيادة
            - ADMIN: يمكنه عرض عيادته فقط
            """
    )
    public ResponseEntity<ApiResponse<ClinicResponse>> getClinicById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Fetching clinic {} by user: {}", id, currentUser.getId());

            ClinicResponse clinic = clinicService.getClinicById(id, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب تفاصيل العيادة بنجاح", clinic)
            );
        } catch (Exception e) {
            logger.error("Error fetching clinic {}: ", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(false, "العيادة غير موجودة: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Get clinic statistics
     */
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN')")
    @Operation(
            summary = "📊 إحصائيات العيادة",
            description = """
            الحصول على إحصائيات شاملة للعيادة
            - عدد المرضى والمستخدمين
            - المواعيد والإيرادات
            - معلومات الاشتراك
            """
    )
    public ResponseEntity<ApiResponse<ClinicStatisticsResponse>> getClinicStatistics(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Fetching statistics for clinic {} by user: {}", id, currentUser.getId());

            ClinicStatisticsResponse stats = clinicService.getClinicStatistics(id, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم جلب إحصائيات العيادة بنجاح", stats)
            );
        } catch (Exception e) {
            logger.error("Error fetching clinic statistics: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل جلب الإحصائيات: " + e.getMessage(), null)
            );
        }
    }

    // ===================================================================
    // UPDATE OPERATIONS
    // ===================================================================

    /**
     * Update clinic information
     */
    @PutMapping("/{id}")
    @SystemAdminContext
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ADMIN')")
    @Operation(
            summary = "✏️ تحديث بيانات العيادة",
            description = """
            تحديث معلومات العيادة
            - SYSTEM_ADMIN: يحتاج سياق العيادة + يمكنه تحديث أي عيادة
            - ADMIN: يمكنه تحديث عيادته فقط
            
            Headers المطلوبة لـ SYSTEM_ADMIN:
            - X-Acting-Clinic-Id: معرف العيادة
            - X-Acting-Reason: سبب التحديث
            """
    )
    public ResponseEntity<ApiResponse<ClinicResponse>> updateClinic(
            @Valid @RequestBody UpdateClinicRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            // Log if SYSTEM_ADMIN is acting with context
            if (UserRole.SYSTEM_ADMIN.name().equals(currentUser.getRole())) {
                ClinicContextService.ClinicContextInfo contextInfo =
                        clinicContextService.getCurrentContext(currentUser);
                logger.info("SYSTEM_ADMIN is updating clinic with clinic context. ActingClinicId: {}, Reason: {}",
                        contextInfo.getActingAsClinicId(), contextInfo.getReason());
            }

            ClinicResponse clinic = clinicService.updateClinic(request, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تحديث بيانات العيادة بنجاح", clinic)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل تحديث العيادة: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Update clinic subscription (SYSTEM_ADMIN only)
     */
    @PutMapping("/{id}/subscription")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "💳 تحديث اشتراك العيادة",
            description = """
            تحديث خطة اشتراك العيادة (SYSTEM_ADMIN فقط)
            """
    )
    public ResponseEntity<ApiResponse<ClinicResponse>> updateSubscription(
            @Parameter(description = "معرف العيادة", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubscriptionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Updating subscription for clinic {} by SYSTEM_ADMIN: {}",
                    id, currentUser.getId());

            ClinicResponse clinic = clinicService.updateSubscription(id, request, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تحديث الاشتراك بنجاح", clinic)
            );
        } catch (Exception e) {
            logger.error("Error updating subscription for clinic {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل تحديث الاشتراك: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Activate clinic (SYSTEM_ADMIN only)
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "✅ تفعيل العيادة",
            description = """
            تفعيل عيادة غير نشطة (SYSTEM_ADMIN فقط)
            """
    )
    public ResponseEntity<ApiResponse<ClinicResponse>> activateClinic(
            @Parameter(description = "معرف العيادة", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Activating clinic {} by SYSTEM_ADMIN: {}", id, currentUser.getId());

            ClinicResponse clinic = clinicService.activateClinic(id, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تفعيل العيادة بنجاح", clinic)
            );
        } catch (Exception e) {
            logger.error("Error activating clinic {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل تفعيل العيادة: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Deactivate clinic (SYSTEM_ADMIN only)
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "🚫 إلغاء تفعيل العيادة",
            description = """
            إلغاء تفعيل عيادة نشطة (SYSTEM_ADMIN فقط)
            """
    )
    public ResponseEntity<ApiResponse<ClinicResponse>> deactivateClinic(
            @Parameter(description = "معرف العيادة", example = "1")
            @PathVariable Long id,
            @Parameter(description = "سبب إلغاء التفعيل", required = true)
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Deactivating clinic {} by SYSTEM_ADMIN: {} - Reason: {}",
                    id, currentUser.getId(), reason);

            ClinicResponse clinic = clinicService.deactivateClinic(id, reason, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم إلغاء تفعيل العيادة بنجاح", clinic)
            );
        } catch (Exception e) {
            logger.error("Error deactivating clinic {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل إلغاء تفعيل العيادة: " + e.getMessage(), null)
            );
        }
    }

    // ===================================================================
    // DELETE OPERATIONS
    // ===================================================================

    /**
     * Delete clinic (SYSTEM_ADMIN only)
     * Soft delete - clinic is deactivated, not removed
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "❌ حذف العيادة",
            description = """
            حذف عيادة (إلغاء تفعيل دائم) - SYSTEM_ADMIN فقط
            - لا يمكن حذف عيادة بها بيانات نشطة
            - الحذف هو إلغاء تفعيل دائم وليس حذف من قاعدة البيانات
            """
    )
    public ResponseEntity<ApiResponse<Void>> deleteClinic(
            @Parameter(description = "معرف العيادة", example = "1")
            @PathVariable Long id,
            @Parameter(description = "سبب الحذف", required = true)
            @RequestParam String reason,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Deleting clinic {} by SYSTEM_ADMIN: {} - Reason: {}",
                    id, currentUser.getId(), reason);

            clinicService.deleteClinic(id, reason, currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم حذف العيادة بنجاح", null)
            );
        } catch (Exception e) {
            logger.error("Error deleting clinic {}: ", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل حذف العيادة: " + e.getMessage(), null)
            );
        }
    }

    // ===================================================================
    // ADDITIONAL OPERATIONS
    // ===================================================================

    /**
     * Get clinics with expiring subscriptions
     */
    @GetMapping("/expiring-subscriptions")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
            summary = "⏰ العيادات ذات الاشتراكات المنتهية قريباً",
            description = "الحصول على قائمة العيادات التي ستنتهي اشتراكاتها قريباً (SYSTEM_ADMIN فقط)"
    )
    public ResponseEntity<ApiResponse<List<ClinicResponse>>> getExpiringSoonClinics(
            @Parameter(description = "عدد الأيام المتبقية", example = "30")
            @RequestParam(defaultValue = "30") int daysAhead,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            logger.info("Fetching clinics with expiring subscriptions within {} days", daysAhead);

            List<ClinicResponse> clinics = clinicService.getClinicsWithExpiringSoonSubscriptions(daysAhead);

            String message = String.format("تم جلب %d عيادة بإشتراكات منتهية خلال %d يوم",
                    clinics.size(), daysAhead);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, message, clinics)
            );
        } catch (Exception e) {
            logger.error("Error fetching expiring clinics: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ApiResponse<>(false, "فشل جلب العيادات: " + e.getMessage(), null)
            );
        }
    }
}