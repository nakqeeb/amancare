// =============================================================================
// Create Announcement Request DTO
// =============================================================================

package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.AnnouncementPriority;
import com.nakqeeb.amancare.entity.AnnouncementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "طلب إنشاء إعلان جديد")
public class CreateAnnouncementRequest {

    @NotNull(message = "نوع الإعلان مطلوب")
    @Schema(description = "نوع الإعلان", example = "GENERAL")
    private AnnouncementType type;

    @NotBlank(message = "عنوان الإعلان مطلوب")
    @Size(min = 3, max = 200, message = "عنوان الإعلان يجب أن يكون بين 3 و 200 حرف")
    @Schema(description = "عنوان الإعلان", example = "إعلان مهم")
    private String title;

    @NotBlank(message = "نص الإعلان مطلوب")
    @Size(min = 10, max = 2000, message = "نص الإعلان يجب أن يكون بين 10 و 2000 حرف")
    @Schema(description = "نص الإعلان", example = "تفاصيل الإعلان هنا...")
    private String message;

    @Schema(description = "معرف العيادة (اختياري، null للإعلان العام)")
    private Long clinicId;

    @Schema(description = "معرف الطبيب (اختياري)")
    private Long doctorId;

    @Schema(description = "أولوية الإعلان", example = "MEDIUM")
    private AnnouncementPriority priority = AnnouncementPriority.MEDIUM;

    @NotNull(message = "تاريخ البداية مطلوب")
    @Schema(description = "تاريخ بداية عرض الإعلان", example = "2025-10-15")
    private LocalDate startDate;

    @Schema(description = "تاريخ نهاية عرض الإعلان (اختياري)", example = "2025-10-30")
    private LocalDate endDate;

    @Schema(description = "حالة التفعيل", example = "true")
    private Boolean isActive = true;

    @Schema(description = "رابط الصورة (اختياري)")
    @Size(max = 500, message = "رابط الصورة يجب ألا يتجاوز 500 حرف")
    private String imageUrl;

    @Schema(description = "رابط الإجراء (اختياري)")
    @Size(max = 500, message = "رابط الإجراء يجب ألا يتجاوز 500 حرف")
    private String actionUrl;

    @Schema(description = "نص زر الإجراء (اختياري)")
    @Size(max = 100, message = "نص زر الإجراء يجب ألا يتجاوز 100 حرف")
    private String actionText;
}