package com.nakqeeb.amancare.dto.response.healthrecords;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordStatisticsResponse {

    private Long totalRecords;
    private Long completedRecords;
    private Long draftRecords;
    private Long reviewedRecords;
    private Long lockedRecords;

    private Long recordsThisMonth;
    private Long recordsThisWeek;
    private Long recordsToday;

    private Long totalDiagnoses;
    private Long totalPrescriptions;
    private Long totalLabTests;
    private Long totalRadiologyTests;
    private Long totalProcedures;
    private Long totalReferrals;

    // Most common diagnoses
    private List<DiagnosisFrequencyResponse> commonDiagnoses;

    // Most prescribed medications
    private List<MedicationFrequencyResponse> commonMedications;

    // Visit type distribution
    private List<VisitTypeStatsResponse> visitTypeStats;
}
