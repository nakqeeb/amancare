package com.nakqeeb.amancare.dto.request.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.RecordStatus;
import com.nakqeeb.amancare.entity.healthrecords.VisitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMedicalRecordRequest {

    private VisitType visitType;

    @Valid
    private VitalSignsDto vitalSigns;

    @Size(max = 1000, message = "الشكوى الرئيسية يجب أن تكون أقل من 1000 حرف")
    private String chiefComplaint;

    @Size(max = 2000, message = "تاريخ المرض الحالي يجب أن يكون أقل من 2000 حرف")
    private String presentIllness;

    @Size(max = 1500, message = "التاريخ المرضي يجب أن يكون أقل من 1500 حرف")
    private String pastMedicalHistory;

    @Size(max = 1500, message = "التاريخ العائلي يجب أن يكون أقل من 1500 حرف")
    private String familyHistory;

    @Size(max = 1000, message = "التاريخ الاجتماعي يجب أن يكون أقل من 1000 حرف")
    private String socialHistory;

    private List<String> allergies;

    private List<String> currentMedications;

    @Size(max = 2000, message = "الفحص السريري يجب أن يكون أقل من 2000 حرف")
    private String physicalExamination;

    @Size(max = 2000, message = "فحص الأجهزة يجب أن يكون أقل من 2000 حرف")
    private String systemicExamination;

    @Valid
    private List<CreateDiagnosisDto> diagnosis;

    @Size(max = 2000, message = "خطة العلاج يجب أن تكون أقل من 2000 حرف")
    private String treatmentPlan;

    @Valid
    private List<CreatePrescriptionDto> prescriptions;

    @Valid
    private List<CreateLabTestDto> labTests;

    @Valid
    private List<CreateRadiologyTestDto> radiologyTests;

    @Valid
    private List<CreateMedicalProcedureDto> procedures;

    private LocalDate followUpDate;

    @Size(max = 1500, message = "تعليمات المتابعة يجب أن تكون أقل من 1500 حرف")
    private String followUpInstructions;

    @Valid
    private List<CreateReferralDto> referrals;

    @Size(max = 1000, message = "الملاحظات يجب أن تكون أقل من 1000 حرف")
    private String notes;

    private Boolean isConfidential;

    private RecordStatus status;
}
