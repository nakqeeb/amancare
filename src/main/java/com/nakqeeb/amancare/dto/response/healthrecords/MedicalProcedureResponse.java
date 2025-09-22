package com.nakqeeb.amancare.dto.response.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.MedicalProcedure;
import com.nakqeeb.amancare.entity.healthrecords.ProcedureCategory;
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
public class MedicalProcedureResponse {

    private Long id;
    private String procedureName;
    private String procedureCode;
    private ProcedureCategory category;
    private String categoryArabic;
    private String description;
    private LocalDate performedDate;
    private String performedBy;
    private String complications;
    private String outcome;
    private String notes;
    private LocalDateTime createdAt;

    public static MedicalProcedureResponse fromEntity(MedicalProcedure procedure) {
        return MedicalProcedureResponse.builder()
                .id(procedure.getId())
                .procedureName(procedure.getProcedureName())
                .procedureCode(procedure.getProcedureCode())
                .category(procedure.getCategory())
                .categoryArabic(procedure.getCategory() != null ?
                        procedure.getCategory().getArabicName() : null)
                .description(procedure.getDescription())
                .performedDate(procedure.getPerformedDate())
                .performedBy(procedure.getPerformedBy())
                .complications(procedure.getComplications())
                .outcome(procedure.getOutcome())
                .notes(procedure.getNotes())
                .createdAt(procedure.getCreatedAt())
                .build();
    }
}
