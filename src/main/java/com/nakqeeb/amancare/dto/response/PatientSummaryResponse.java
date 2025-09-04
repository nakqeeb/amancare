package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.BloodType;
import com.nakqeeb.amancare.entity.Gender;
import com.nakqeeb.amancare.entity.Patient;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * ملخص المريض (لقوائم المرضى)
 */
@Schema(description = "ملخص بيانات المريض")
public class PatientSummaryResponse {

    @Schema(description = "معرف المريض", example = "1")
    private Long id;

    @Schema(description = "رقم المريض في العيادة", example = "P001")
    private String patientNumber;

    @Schema(description = "الاسم الكامل", example = "محمد أحمد")
    private String fullName;

    @Schema(description = "العمر", example = "33")
    private Integer age;

    @Schema(description = "الجنس", example = "MALE")
    private Gender gender;

    @Schema(description = "رقم الهاتف", example = "771234567")
    private String phone;

    @Schema(description = "فصيلة الدم", example = "O_POSITIVE")
    private BloodType bloodType;

    @Schema(description = "آخر زيارة", example = "2024-01-10T09:00:00")
    private LocalDateTime lastVisit;

    @Schema(description = "حالة النشاط", example = "true")
    private boolean active;

    // Constructors
    public PatientSummaryResponse() {}

    /**
     * إنشاء PatientSummaryResponse من Patient entity
     */
    public static PatientSummaryResponse fromPatient(Patient patient) {
        PatientSummaryResponse summary = new PatientSummaryResponse();
        summary.setId(patient.getId());
        summary.setPatientNumber(patient.getPatientNumber());
        summary.setFullName(patient.getFullName());
        summary.setAge(patient.getAge());
        summary.setGender(patient.getGender());
        summary.setPhone(patient.getPhone());
        summary.setActive(patient.getIsActive());
        summary.setBloodType(patient.getBloodType());
        // آخر زيارة يمكن استخراجها من Medical Records أو Appointments
        return summary;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientNumber() { return patientNumber; }
    public void setPatientNumber(String patientNumber) { this.patientNumber = patientNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public BloodType getBloodType() {
        return bloodType;
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public LocalDateTime getLastVisit() { return lastVisit; }
    public void setLastVisit(LocalDateTime lastVisit) { this.lastVisit = lastVisit; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}