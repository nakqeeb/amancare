package com.nakqeeb.amancare.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
/**
 * معلومات المريض في الموعد
 */
@Schema(description = "معلومات المريض في الموعد")
public class AppointmentPatientInfo {
    @Schema(description = "معرف المريض", example = "1")
    private Long id;

    @Schema(description = "رقم المريض", example = "P202401001")
    private String patientNumber;

    @Schema(description = "اسم المريض الكامل", example = "محمد أحمد")
    private String fullName;

    @Schema(description = "رقم الهاتف", example = "771234567")
    private String phone;

    @Schema(description = "العمر", example = "35")
    private Integer age;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientNumber() { return patientNumber; }
    public void setPatientNumber(String patientNumber) { this.patientNumber = patientNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
