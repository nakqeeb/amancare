package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.BloodType;
import com.nakqeeb.amancare.entity.Gender;
import com.nakqeeb.amancare.entity.Patient;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * استجابة بيانات المريض
 */
@Schema(description = "بيانات المريض")
public class PatientResponse {

    @Schema(description = "معرف المريض", example = "1")
    private Long id;

    @Schema(description = "رقم المريض في العيادة", example = "P001")
    private String patientNumber;

    @Schema(description = "الاسم الأول", example = "محمد")
    private String firstName;

    @Schema(description = "الاسم الأخير", example = "أحمد")
    private String lastName;

    @Schema(description = "الاسم الكامل", example = "محمد أحمد")
    private String fullName;

    @Schema(description = "تاريخ الميلاد", example = "1990-05-15")
    private LocalDate dateOfBirth;

    @Schema(description = "العمر", example = "33")
    private Integer age;

    @Schema(description = "الجنس", example = "MALE")
    private Gender gender;

    @Schema(description = "رقم الهاتف", example = "771234567")
    private String phone;

    @Schema(description = "البريد الإلكتروني", example = "patient@example.com")
    private String email;

    @Schema(description = "العنوان", example = "حي الصافية، شارع الجامعة، صنعاء")
    private String address;

    @Schema(description = "اسم جهة الاتصال في حالات الطوارئ", example = "فاطمة أحمد")
    private String emergencyContactName;

    @Schema(description = "رقم جهة الاتصال في حالات الطوارئ", example = "773456789")
    private String emergencyContactPhone;

    @Schema(description = "فصيلة الدم", example = "O_POSITIVE")
    private BloodType bloodType;

    @Schema(description = "الحساسيات", example = "حساسية من البنسلين والمكسرات")
    private String allergies;

    @Schema(description = "الأمراض المزمنة", example = "ارتفاع ضغط الدم، السكري")
    private String chronicDiseases;

    @Schema(description = "ملاحظات إضافية", example = "يفضل المواعيد الصباحية")
    private String notes;

    @Schema(description = "حالة النشاط", example = "true")
    private boolean active;

    @Schema(description = "تاريخ الإنشاء", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "تاريخ آخر تحديث", example = "2024-01-16T14:20:00")
    private LocalDateTime updatedAt;

    // Constructors
    public PatientResponse() {}

    /**
     * إنشاء PatientResponse من Patient entity
     */
    public static PatientResponse fromPatient(Patient patient) {
        PatientResponse response = new PatientResponse();
        response.setId(patient.getId());
        response.setPatientNumber(patient.getPatientNumber());
        response.setFirstName(patient.getFirstName());
        response.setLastName(patient.getLastName());
        response.setFullName(patient.getFullName());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setAge(patient.getAge());
        response.setGender(patient.getGender());
        response.setPhone(patient.getPhone());
        response.setEmail(patient.getEmail());
        response.setAddress(patient.getAddress());
        response.setEmergencyContactName(patient.getEmergencyContactName());
        response.setEmergencyContactPhone(patient.getEmergencyContactPhone());
        response.setBloodType(patient.getBloodType());
        response.setAllergies(patient.getAllergies());
        response.setChronicDiseases(patient.getChronicDiseases());
        response.setNotes(patient.getNotes());
        response.setActive(patient.getIsActive());
        response.setCreatedAt(patient.getCreatedAt());
        response.setUpdatedAt(patient.getUpdatedAt());
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientNumber() { return patientNumber; }
    public void setPatientNumber(String patientNumber) { this.patientNumber = patientNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public BloodType getBloodType() { return bloodType; }
    public void setBloodType(BloodType bloodType) { this.bloodType = bloodType; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getChronicDiseases() { return chronicDiseases; }
    public void setChronicDiseases(String chronicDiseases) { this.chronicDiseases = chronicDiseases; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}