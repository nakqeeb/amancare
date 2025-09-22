// =============================================================================
// Medical Record Entity - كيان السجل الطبي
// src/main/java/com/nakqeeb/amancare/entity/MedicalRecord.java
// =============================================================================

package com.nakqeeb.amancare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.nakqeeb.amancare.entity.healthrecords.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Patient relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnore
    private Patient patient;

    @Column(name = "patient_id", insertable = false, updatable = false)
    private Long patientId;

    // Appointment relationship (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    @JsonIgnore
    private Appointment appointment;

    @Column(name = "appointment_id", insertable = false, updatable = false)
    private Long appointmentId;

    // Doctor relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnore
    private User doctor;

    @Column(name = "doctor_id", insertable = false, updatable = false)
    private Long doctorId;

    // Clinic relationship (for multi-tenant support)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    @JsonIgnore
    private Clinic clinic;

    @Column(name = "clinic_id", insertable = false, updatable = false)
    private Long clinicId;

    // Basic Information
    @Column(name = "visit_date", nullable = false)
    @NotNull(message = "تاريخ الزيارة مطلوب")
    private LocalDate visitDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", nullable = false, length = 50)
    @NotNull(message = "نوع الزيارة مطلوب")
    private VisitType visitType;

    // Vital Signs (stored as JSON in database)
    @Embedded
    private VitalSigns vitalSigns;

    // Medical Information
    @Column(name = "chief_complaint", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "الشكوى الرئيسية مطلوبة")
    @Size(max = 1000, message = "الشكوى الرئيسية يجب أن تكون أقل من 1000 حرف")
    private String chiefComplaint;

    @Column(name = "present_illness", columnDefinition = "TEXT")
    @Size(max = 2000, message = "تاريخ المرض الحالي يجب أن يكون أقل من 2000 حرف")
    private String presentIllness;

    @Column(name = "past_medical_history", columnDefinition = "TEXT")
    @Size(max = 1500, message = "التاريخ المرضي يجب أن يكون أقل من 1500 حرف")
    private String pastMedicalHistory;

    @Column(name = "family_history", columnDefinition = "TEXT")
    @Size(max = 1500, message = "التاريخ العائلي يجب أن يكون أقل من 1500 حرف")
    private String familyHistory;

    @Column(name = "social_history", columnDefinition = "TEXT")
    @Size(max = 1000, message = "التاريخ الاجتماعي يجب أن يكون أقل من 1000 حرف")
    private String socialHistory;

    @ElementCollection
    @CollectionTable(name = "medical_record_allergies", joinColumns = @JoinColumn(name = "medical_record_id"))
    @Column(name = "allergy", length = 255)
    private List<String> allergies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "medical_record_medications", joinColumns = @JoinColumn(name = "medical_record_id"))
    @Column(name = "medication", length = 255)
    private List<String> currentMedications = new ArrayList<>();

    // Physical Examination
    @Column(name = "physical_examination", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "الفحص السريري مطلوب")
    @Size(max = 2000, message = "الفحص السريري يجب أن يكون أقل من 2000 حرف")
    private String physicalExamination;

    @Column(name = "systemic_examination", columnDefinition = "TEXT")
    @Size(max = 2000, message = "فحص الأجهزة يجب أن يكون أقل من 2000 حرف")
    private String systemicExamination;

    // Diagnosis (One-to-Many relationship)
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Diagnosis> diagnosis = new ArrayList<>();

    // Treatment Plan
    @Column(name = "treatment_plan", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "خطة العلاج مطلوبة")
    @Size(max = 2000, message = "خطة العلاج يجب أن تكون أقل من 2000 حرف")
    private String treatmentPlan;

    // Prescriptions (One-to-Many relationship)
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Prescription> prescriptions = new ArrayList<>();

    // Lab Tests (One-to-Many relationship)
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<LabTest> labTests = new ArrayList<>();

    // Radiology Tests (One-to-Many relationship)
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<RadiologyTest> radiologyTests = new ArrayList<>();

    // Medical Procedures (One-to-Many relationship)
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<MedicalProcedure> procedures = new ArrayList<>();

    // Follow-up Information
    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    @Size(max = 1500, message = "تعليمات المتابعة يجب أن تكون أقل من 1500 حرف")
    private String followUpInstructions;

    // Referrals (One-to-Many relationship)
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Referral> referrals = new ArrayList<>();

    // Administrative Information
    @Column(name = "notes", columnDefinition = "TEXT")
    @Size(max = 1000, message = "الملاحظات يجب أن تكون أقل من 1000 حرف")
    private String notes;

    @Column(name = "is_confidential", nullable = false)
    @Builder.Default
    private Boolean isConfidential = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "حالة السجل مطلوبة")
    @Builder.Default
    private RecordStatus status = RecordStatus.DRAFT;

    // Audit Information
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Helper methods for managing bidirectional relationships
    public void addDiagnosis(Diagnosis diagnosis) {
        this.diagnosis.add(diagnosis);
        diagnosis.setMedicalRecord(this);
    }

    public void removeDiagnosis(Diagnosis diagnosis) {
        this.diagnosis.remove(diagnosis);
        diagnosis.setMedicalRecord(null);
    }

    public void addPrescription(Prescription prescription) {
        this.prescriptions.add(prescription);
        prescription.setMedicalRecord(this);
    }

    public void removePrescription(Prescription prescription) {
        this.prescriptions.remove(prescription);
        prescription.setMedicalRecord(null);
    }

    public void addLabTest(LabTest labTest) {
        this.labTests.add(labTest);
        labTest.setMedicalRecord(this);
    }

    public void removeLabTest(LabTest labTest) {
        this.labTests.remove(labTest);
        labTest.setMedicalRecord(null);
    }

    public void addRadiologyTest(RadiologyTest radiologyTest) {
        this.radiologyTests.add(radiologyTest);
        radiologyTest.setMedicalRecord(this);
    }

    public void removeRadiologyTest(RadiologyTest radiologyTest) {
        this.radiologyTests.remove(radiologyTest);
        radiologyTest.setMedicalRecord(null);
    }

    public void addProcedure(MedicalProcedure procedure) {
        this.procedures.add(procedure);
        procedure.setMedicalRecord(this);
    }

    public void removeProcedure(MedicalProcedure procedure) {
        this.procedures.remove(procedure);
        procedure.setMedicalRecord(null);
    }

    public void addReferral(Referral referral) {
        this.referrals.add(referral);
        referral.setMedicalRecord(this);
    }

    public void removeReferral(Referral referral) {
        this.referrals.remove(referral);
        referral.setMedicalRecord(null);
    }
}