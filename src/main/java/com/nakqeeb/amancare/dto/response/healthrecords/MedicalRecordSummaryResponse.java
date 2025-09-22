package com.nakqeeb.amancare.dto.response.healthrecords;

import com.nakqeeb.amancare.entity.MedicalRecord;
import com.nakqeeb.amancare.entity.healthrecords.Diagnosis;
import com.nakqeeb.amancare.entity.healthrecords.RecordStatus;
import com.nakqeeb.amancare.entity.healthrecords.VisitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordSummaryResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientNumber;
    private Long doctorId;
    private String doctorName;
    private LocalDate visitDate;
    private VisitType visitType;
    private String visitTypeArabic;
    private String chiefComplaint;
    private String primaryDiagnosis;
    private RecordStatus status;
    private String statusArabic;
    private Boolean isConfidential;
    private LocalDateTime createdAt;

    // Factory method to create from entity
    public static MedicalRecordSummaryResponse fromEntity(MedicalRecord medicalRecord) {
        String primaryDiagnosis = null;
        if (medicalRecord.getDiagnosis() != null) {
            primaryDiagnosis = medicalRecord.getDiagnosis().stream()
                    .filter(d -> d.getIsPrimary())
                    .findFirst()
                    .map(Diagnosis::getDescription)
                    .orElse(null);
        }

        return MedicalRecordSummaryResponse.builder()
                .id(medicalRecord.getId())
                .patientId(medicalRecord.getPatientId())
                .patientName(medicalRecord.getPatient() != null ?
                        medicalRecord.getPatient().getFirstName() + " " + medicalRecord.getPatient().getLastName() : null)
                .patientNumber(medicalRecord.getPatient() != null ?
                        medicalRecord.getPatient().getPatientNumber() : null)
                .doctorId(medicalRecord.getDoctorId())
                .doctorName(medicalRecord.getDoctor() != null ?
                        medicalRecord.getDoctor().getFirstName() + " " + medicalRecord.getDoctor().getLastName() : null)
                .visitDate(medicalRecord.getVisitDate())
                .visitType(medicalRecord.getVisitType())
                .visitTypeArabic(medicalRecord.getVisitType() != null ?
                        medicalRecord.getVisitType().getArabicName() : null)
                .chiefComplaint(medicalRecord.getChiefComplaint())
                .primaryDiagnosis(primaryDiagnosis)
                .status(medicalRecord.getStatus())
                .statusArabic(medicalRecord.getStatus() != null ?
                        medicalRecord.getStatus().getArabicName() : null)
                .isConfidential(medicalRecord.getIsConfidential())
                .createdAt(medicalRecord.getCreatedAt())
                .build();
    }
}
