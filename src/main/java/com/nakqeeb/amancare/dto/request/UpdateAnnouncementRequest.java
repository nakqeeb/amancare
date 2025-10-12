// =============================================================================
// Update Announcement Request DTO
// =============================================================================

package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.AnnouncementPriority;
import com.nakqeeb.amancare.entity.AnnouncementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "طلب تحديث إعلان")
public class UpdateAnnouncementRequest {

    @Schema(description = "نوع الإعلان")
    private AnnouncementType type;

    @Size(min = 3, max = 200, message = "عنوان الإعلان يجب أن يكون بين 3 و 200 حرف")
    @Schema(description = "عنوان الإعلان")
    private String title;

    @Size(min = 10, max = 2000, message = "نص الإعلان يجب أن يكون بين 10 و 2000 حرف")
    @Schema(description = "نص الإعلان")
    private String message;

    @Schema(description = "معرف العيادة")
    private Long clinicId;

    @Schema(description = "معرف الطبيب")
    private Long doctorId;

    @Schema(description = "أولوية الإعلان")
    private AnnouncementPriority priority;

    @Schema(description = "تاريخ بداية عرض الإعلان")
    private LocalDate startDate;

    @Schema(description = "تاريخ نهاية عرض الإعلان")
    private LocalDate endDate;

    @Schema(description = "حالة التفعيل")
    private Boolean isActive;

    @Size(max = 500, message = "رابط الصورة يجب ألا يتجاوز 500 حرف")
    @Schema(description = "رابط الصورة")
    private String imageUrl;

    @Size(max = 500, message = "رابط الإجراء يجب ألا يتجاوز 500 حرف")
    @Schema(description = "رابط الإجراء")
    private String actionUrl;

    @Size(max = 100, message = "نص زر الإجراء يجب ألا يتجاوز 100 حرف")
    @Schema(description = "نص زر الإجراء")
    private String actionText;
}