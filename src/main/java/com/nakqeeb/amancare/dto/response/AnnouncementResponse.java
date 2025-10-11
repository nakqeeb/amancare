// src/main/java/com/nakqeeb/amancare/dto/response/AnnouncementResponse.java

package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.Announcement;
import com.nakqeeb.amancare.entity.AnnouncementPriority;
import com.nakqeeb.amancare.entity.AnnouncementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Announcement response")
public class AnnouncementResponse {

    @Schema(description = "معرف الإعلان")
    private Long id;

    @Schema(description = "نوع الإعلان")
    private AnnouncementType type;

    @Schema(description = "عنوان الإعلان")
    private String title;

    @Schema(description = "نص الإعلان")
    private String message;

    @Schema(description = "معرف العيادة")
    private Long clinicId;

    @Schema(description = "اسم العيادة")
    private String clinicName;

    @Schema(description = "معرف الطبيب")
    private Long doctorId;

    @Schema(description = "اسم الطبيب")
    private String doctorName;

    @Schema(description = "التخصص")
    private String specialization;

    @Schema(description = "الأولوية")
    private AnnouncementPriority priority;

    @Schema(description = "تاريخ البداية")
    private LocalDate startDate;

    @Schema(description = "تاريخ النهاية")
    private LocalDate endDate;

    @Schema(description = "هل نشط")
    private Boolean isActive;

    @Schema(description = "رابط الصورة")
    private String imageUrl;

    @Schema(description = "رابط الإجراء")
    private String actionUrl;

    @Schema(description = "نص زر الإجراء")
    private String actionText;

    public static AnnouncementResponse fromEntity(Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId());
        response.setType(announcement.getType());
        response.setTitle(announcement.getTitle());
        response.setMessage(announcement.getMessage());

        if (announcement.getClinic() != null) {
            response.setClinicId(announcement.getClinic().getId());
            response.setClinicName(announcement.getClinic().getName());
        }

        if (announcement.getDoctor() != null) {
            response.setDoctorId(announcement.getDoctor().getId());
            response.setDoctorName(announcement.getDoctor().getFullName());
            response.setSpecialization(announcement.getDoctor().getSpecialization());
        }

        response.setPriority(announcement.getPriority());
        response.setStartDate(announcement.getStartDate());
        response.setEndDate(announcement.getEndDate());
        response.setIsActive(announcement.getIsActive());
        response.setImageUrl(announcement.getImageUrl());
        response.setActionUrl(announcement.getActionUrl());
        response.setActionText(announcement.getActionText());

        return response;
    }
}