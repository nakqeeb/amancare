package com.nakqeeb.amancare.dto.response.healthrecords;

import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.entity.healthrecords.RecordStatus;
import com.nakqeeb.amancare.entity.healthrecords.VisitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// =============================================================================
// MAIN RESPONSE DTOs
// =============================================================================

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordResponse {

    private Long id;

    // Patient Information
    private Long patientId;
    private String patientName;
    private String patientNumber;

    // Appointment Information
    private Long appointmentId;
    private String appointmentType;

    // Doctor Information
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;

    // Clinic Information
    private Long clinicId;
    private String clinicName;

    // Basic Information
    private LocalDate visitDate;
    private VisitType visitType;
    private String visitTypeArabic;

    // Vital Signs
    private VitalSignsResponse vitalSigns;

    // Medical Information
    private String chiefComplaint;
    private String presentIllness;
    private String pastMedicalHistory;
    private String familyHistory;
    private String socialHistory;
    private List<String> allergies;
    private List<String> currentMedications;

    // Physical Examination
    private String physicalExamination;
    private String systemicExamination;

    // Diagnosis & Treatment
    private List<DiagnosisResponse> diagnosis;
    private String treatmentPlan;
    private List<PrescriptionResponse> prescriptions;
    private List<LabTestResponse> labTests;
    private List<RadiologyTestResponse> radiologyTests;
    private List<MedicalProcedureResponse> procedures;

    // Follow-up
    private LocalDate followUpDate;
    private String followUpInstructions;
    private List<ReferralResponse> referrals;

    // Administrative
    private String notes;
    private Boolean isConfidential;
    private RecordStatus status;
    private String statusArabic;

    // Audit Information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Factory method to create from entity
    public static MedicalRecordResponse fromEntity(MedicalRecord medicalRecord) {
        return MedicalRecordResponse.builder()
                .id(medicalRecord.getId())
                .patientId(medicalRecord.getPatientId())
                .patientName(medicalRecord.getPatient() != null ?
                        medicalRecord.getPatient().getFirstName() + " " + medicalRecord.getPatient().getLastName() : null)
                .patientNumber(medicalRecord.getPatient() != null ?
                        medicalRecord.getPatient().getPatientNumber() : null)
                .appointmentId(medicalRecord.getAppointmentId())
                .doctorId(medicalRecord.getDoctorId())
                .doctorName(medicalRecord.getDoctor() != null ?
                        medicalRecord.getDoctor().getFirstName() + " " + medicalRecord.getDoctor().getLastName() : null)
                .doctorSpecialization(medicalRecord.getDoctor() != null ?
                        medicalRecord.getDoctor().getSpecialization() : null)
                .clinicId(medicalRecord.getClinicId())
                .clinicName(medicalRecord.getClinic() != null ?
                        medicalRecord.getClinic().getName() : null)
                .visitDate(medicalRecord.getVisitDate())
                .visitType(medicalRecord.getVisitType())
                .visitTypeArabic(medicalRecord.getVisitType() != null ?
                        medicalRecord.getVisitType().getArabicName() : null)
                .vitalSigns(medicalRecord.getVitalSigns() != null ?
                        VitalSignsResponse.fromEmbeddable(medicalRecord.getVitalSigns()) : null)
                .chiefComplaint(medicalRecord.getChiefComplaint())
                .presentIllness(medicalRecord.getPresentIllness())
                .pastMedicalHistory(medicalRecord.getPastMedicalHistory())
                .familyHistory(medicalRecord.getFamilyHistory())
                .socialHistory(medicalRecord.getSocialHistory())
                .allergies(medicalRecord.getAllergies())
                .currentMedications(medicalRecord.getCurrentMedications())
                .physicalExamination(medicalRecord.getPhysicalExamination())
                .systemicExamination(medicalRecord.getSystemicExamination())
                .diagnosis(medicalRecord.getDiagnosis() != null ?
                        medicalRecord.getDiagnosis().stream()
                                .map(DiagnosisResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .treatmentPlan(medicalRecord.getTreatmentPlan())
                .prescriptions(medicalRecord.getPrescriptions() != null ?
                        medicalRecord.getPrescriptions().stream()
                                .map(PrescriptionResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .labTests(medicalRecord.getLabTests() != null ?
                        medicalRecord.getLabTests().stream()
                                .map(LabTestResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .radiologyTests(medicalRecord.getRadiologyTests() != null ?
                        medicalRecord.getRadiologyTests().stream()
                                .map(RadiologyTestResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .procedures(medicalRecord.getProcedures() != null ?
                        medicalRecord.getProcedures().stream()
                                .map(MedicalProcedureResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .followUpDate(medicalRecord.getFollowUpDate())
                .followUpInstructions(medicalRecord.getFollowUpInstructions())
                .referrals(medicalRecord.getReferrals() != null ?
                        medicalRecord.getReferrals().stream()
                                .map(ReferralResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .notes(medicalRecord.getNotes())
                .isConfidential(medicalRecord.getIsConfidential())
                .status(medicalRecord.getStatus())
                .statusArabic(medicalRecord.getStatus() != null ?
                        medicalRecord.getStatus().getArabicName() : null)
                .createdAt(medicalRecord.getCreatedAt())
                .updatedAt(medicalRecord.getUpdatedAt())
                .createdBy(medicalRecord.getCreatedBy())
                .updatedBy(medicalRecord.getUpdatedBy())
                .build();
    }
}