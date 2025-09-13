//package com.nakqeeb.amancare.dto.response;
//
//import com.nakqeeb.amancare.entity.BloodType;
//import com.nakqeeb.amancare.entity.Gender;
//import com.nakqeeb.amancare.entity.Patient;
//import io.swagger.v3.oas.annotations.media.Schema;
//
//import java.time.LocalDateTime;
//
///**
// * ملخص المريض (لقوائم المرضى)
// */
//@Schema(description = "ملخص بيانات المريض")
//public class PatientSummaryResponse {
//
//    @Schema(description = "معرف المريض", example = "1")
//    private Long id;
//
//    @Schema(description = "رقم المريض في العيادة", example = "P001")
//    private String patientNumber;
//
//    @Schema(description = "الاسم الكامل", example = "محمد أحمد")
//    private String fullName;
//
//    @Schema(description = "العمر", example = "33")
//    private Integer age;
//
//    @Schema(description = "الجنس", example = "MALE")
//    private Gender gender;
//
//    @Schema(description = "رقم الهاتف", example = "771234567")
//    private String phone;
//
//    @Schema(description = "فصيلة الدم", example = "O_POSITIVE")
//    private BloodType bloodType;
//
//    @Schema(description = "آخر زيارة", example = "2024-01-10T09:00:00")
//    private LocalDateTime lastVisit;
//
//    @Schema(description = "حالة النشاط", example = "true")
//    private Boolean isActive;
//
//    // Constructors
//    public PatientSummaryResponse() {}
//
//    /**
//     * إنشاء PatientSummaryResponse من Patient entity
//     */
//    public static PatientSummaryResponse fromPatient(Patient patient) {
//        PatientSummaryResponse summary = new PatientSummaryResponse();
//        summary.setId(patient.getId());
//        summary.setPatientNumber(patient.getPatientNumber());
//        summary.setFullName(patient.getFullName());
//        summary.setAge(patient.getAge());
//        summary.setGender(patient.getGender());
//        summary.setPhone(patient.getPhone());
//        summary.setIsActive(patient.getIsActive());
//        summary.setBloodType(patient.getBloodType());
//        // آخر زيارة يمكن استخراجها من Medical Records أو Appointments
//        return summary;
//    }
//
//    // Getters and Setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getPatientNumber() { return patientNumber; }
//    public void setPatientNumber(String patientNumber) { this.patientNumber = patientNumber; }
//
//    public String getFullName() { return fullName; }
//    public void setFullName(String fullName) { this.fullName = fullName; }
//
//    public Integer getAge() { return age; }
//    public void setAge(Integer age) { this.age = age; }
//
//    public Gender getGender() { return gender; }
//    public void setGender(Gender gender) { this.gender = gender; }
//
//    public String getPhone() { return phone; }
//    public void setPhone(String phone) { this.phone = phone; }
//
//    public BloodType getBloodType() {
//        return bloodType;
//    }
//
//    public void setBloodType(BloodType bloodType) {
//        this.bloodType = bloodType;
//    }
//
//    public LocalDateTime getLastVisit() { return lastVisit; }
//    public void setLastVisit(LocalDateTime lastVisit) { this.lastVisit = lastVisit; }
//
//    public Boolean getIsActive() {
//        return isActive;
//    }
//
//    public void setIsActive(Boolean isActive) {
//        this.isActive = isActive;
//    }
//}

// =============================================================================
// PatientSummaryResponse DTO - Enhanced with Additional Fields
// =============================================================================

package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.BloodType;
import com.nakqeeb.amancare.entity.Gender;
import com.nakqeeb.amancare.entity.Patient;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Enhanced Patient Summary Response DTO
 * ملخص بيانات المريض المحسن
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Enhanced patient summary with additional filtering fields")
public class PatientSummaryResponse {

    @Schema(description = "Patient ID", example = "1")
    private Long id;

    @Schema(description = "Patient unique number", example = "P202401001")
    private String patientNumber;

    @Schema(description = "Patient full name", example = "أحمد محمد علي")
    private String fullName;

    @Schema(description = "Patient age", example = "35")
    private Integer age;

    @Schema(description = "Patient gender", example = "MALE")
    private Gender gender;

    @Schema(description = "Phone number", example = "0501234567")
    private String phone;

    @Schema(description = "Blood type", example = "O_POSITIVE")
    private BloodType bloodType;

    @Schema(description = "Last visit date and time")
    private LocalDateTime lastVisit;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Email address", example = "patient@example.com")
    private String email;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "Emergency contact name")
    private String emergencyContactName;

    @Schema(description = "Emergency contact phone")
    private String emergencyContactPhone;

    @Schema(description = "Number of appointments", example = "5")
    private Integer appointmentsCount;

    @Schema(description = "Outstanding balance", example = "500.00")
    private Double outstandingBalance;

    @Schema(description = "Chronic diseases")
    private String chronicDiseases;

    @Schema(description = "Allergies")
    private String allergies;

    // Constructors
    public PatientSummaryResponse() {
    }

    /**
     * Create PatientSummaryResponse from Patient entity
     */
    public static PatientSummaryResponse fromPatient(Patient patient) {
        PatientSummaryResponse response = new PatientSummaryResponse();

        response.setId(patient.getId());
        response.setPatientNumber(patient.getPatientNumber());
        response.setFullName(patient.getFullName());
        response.setPhone(patient.getPhone());
        response.setGender(patient.getGender());
        response.setBloodType(patient.getBloodType());
        response.setIsActive(patient.getIsActive());
        response.setEmail(patient.getEmail());
        response.setAddress(patient.getAddress());
        response.setEmergencyContactName(patient.getEmergencyContactName());
        response.setEmergencyContactPhone(patient.getEmergencyContactPhone());
        response.setChronicDiseases(patient.getChronicDiseases());
        response.setAllergies(patient.getAllergies());

        // Calculate age if date of birth is available
        if (patient.getDateOfBirth() != null) {
            response.setAge(Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears());
        }

        // Set appointment count if available
        if (patient.getAppointments() != null) {
            response.setAppointmentsCount(patient.getAppointments().size());
        }

        // Calculate outstanding balance if invoices are available
        /*if (patient.getInvoices() != null) {
            double totalOutstanding = patient.getInvoices().stream()
                    .filter(invoice -> invoice.getStatus() != null &&
                            (invoice.getStatus().equals("PENDING") || invoice.getStatus().equals("PARTIAL")))
                    .mapToDouble(invoice -> {
                        if (invoice.getRemainingAmount() != null) {
                            return invoice.getRemainingAmount().doubleValue();
                        }
                        return 0.0;
                    })
                    .sum();
            response.setOutstandingBalance(totalOutstanding);
        }*/

        return response;
    }

    /**
     * Create a minimal summary (for list views)
     */
    public static PatientSummaryResponse minimalFromPatient(Patient patient) {
        PatientSummaryResponse response = new PatientSummaryResponse();

        response.setId(patient.getId());
        response.setPatientNumber(patient.getPatientNumber());
        response.setFullName(patient.getFullName());
        response.setPhone(patient.getPhone());
        response.setGender(patient.getGender());
        response.setBloodType(patient.getBloodType());
        response.setIsActive(patient.getIsActive());

        // Calculate age
        if (patient.getDateOfBirth() != null) {
            response.setAge(Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears());
        }

        return response;
    }

    // Builder pattern for flexible construction
    public static class Builder {
        private PatientSummaryResponse response = new PatientSummaryResponse();

        public Builder id(Long id) {
            response.id = id;
            return this;
        }

        public Builder patientNumber(String patientNumber) {
            response.patientNumber = patientNumber;
            return this;
        }

        public Builder fullName(String fullName) {
            response.fullName = fullName;
            return this;
        }

        public Builder age(Integer age) {
            response.age = age;
            return this;
        }

        public Builder gender(Gender gender) {
            response.gender = gender;
            return this;
        }

        public Builder phone(String phone) {
            response.phone = phone;
            return this;
        }

        public Builder bloodType(BloodType bloodType) {
            response.bloodType = bloodType;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            response.isActive = isActive;
            return this;
        }

        public Builder lastVisit(LocalDateTime lastVisit) {
            response.lastVisit = lastVisit;
            return this;
        }

        public PatientSummaryResponse build() {
            return response;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        this.patientNumber = patientNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public LocalDateTime getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(LocalDateTime lastVisit) {
        this.lastVisit = lastVisit;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public Integer getAppointmentsCount() {
        return appointmentsCount;
    }

    public void setAppointmentsCount(Integer appointmentsCount) {
        this.appointmentsCount = appointmentsCount;
    }

    public Double getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(Double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public String getChronicDiseases() {
        return chronicDiseases;
    }

    public void setChronicDiseases(String chronicDiseases) {
        this.chronicDiseases = chronicDiseases;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    // Utility methods
    public String getGenderDisplay() {
        if (gender == null) return "";
        return gender == Gender.MALE ? "ذكر" : "أنثى";
    }

    public String getBloodTypeDisplay() {
        if (bloodType == null) return "";
        return bloodType.getSymbol();
    }

    public String getStatusDisplay() {
        return isActive != null && isActive ? "نشط" : "غير نشط";
    }

    @Override
    public String toString() {
        return "PatientSummaryResponse{" +
                "id=" + id +
                ", patientNumber='" + patientNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", phone='" + phone + '\'' +
                ", bloodType=" + bloodType +
                ", isActive=" + isActive +
                '}';
    }
}