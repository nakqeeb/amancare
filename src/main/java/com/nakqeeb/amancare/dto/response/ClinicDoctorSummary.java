// src/main/java/com/nakqeeb/amancare/dto/response/ClinicDoctorSummary.java

package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Doctor summary for clinic with availability info")
public class ClinicDoctorSummary {

    @Schema(description = "معرف الطبيب")
    private Long doctorId;

    @Schema(description = "اسم الطبيب الكامل")
    private String fullName;

    @Schema(description = "التخصص")
    private String specialization;

    @Schema(description = "الصورة الشخصية")
    private String profileImage;

    @Schema(description = "أيام العمل")
    private List<WorkingDay> workingDays;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkingDay {
        private DayOfWeek day;
        private String dayArabic;
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalTime breakStart;
        private LocalTime breakEnd;
    }

    public static ClinicDoctorSummary fromDoctor(User doctor, List<WorkingDay> workingDays) {
        ClinicDoctorSummary summary = new ClinicDoctorSummary();
        summary.setDoctorId(doctor.getId());
        summary.setFullName(doctor.getFullName());
        summary.setSpecialization(doctor.getSpecialization());
        // summary.setProfileImage(doctor.getProfileImage());
        summary.setWorkingDays(workingDays);
        return summary;
    }
}