package com.nakqeeb.amancare.dto.request.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.RecordStatus;
import com.nakqeeb.amancare.entity.healthrecords.VisitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordSearchCriteria {

    private Long patientId;

    private Long doctorId;

    private VisitType visitType;

    private RecordStatus status;

    private LocalDate visitDateFrom;

    private LocalDate visitDateTo;

    private String searchTerm; // Search in chief complaint, diagnosis, etc.

    private Boolean isConfidential;

    // For SYSTEM_ADMIN - to search across clinics
    private Long clinicId;
}
