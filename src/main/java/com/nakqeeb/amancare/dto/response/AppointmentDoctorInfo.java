package com.nakqeeb.amancare.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
/**
 * معلومات الطبيب في الموعد
 */
@Schema(description = "معلومات الطبيب في الموعد")
public class AppointmentDoctorInfo {
    @Schema(description = "معرف الطبيب", example = "2")
    private Long id;

    @Schema(description = "اسم الطبيب الكامل", example = "د. أحمد محمد")
    private String fullName;

    @Schema(description = "التخصص", example = "طب عام")
    private String specialization;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}