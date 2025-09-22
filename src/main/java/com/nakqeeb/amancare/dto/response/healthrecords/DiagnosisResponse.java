package com.nakqeeb.amancare.dto.response.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.Diagnosis;
import com.nakqeeb.amancare.entity.healthrecords.DiagnosisType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisResponse {

    private Long id;
    private String icdCode;
    private String description;
    private DiagnosisType type;
    private String typeArabic;
    private Boolean isPrimary;
    private String notes;
    private LocalDateTime createdAt;

    public static DiagnosisResponse fromEntity(Diagnosis diagnosis) {
        return DiagnosisResponse.builder()
                .id(diagnosis.getId())
                .icdCode(diagnosis.getIcdCode())
                .description(diagnosis.getDescription())
                .type(diagnosis.getType())
                .typeArabic(diagnosis.getType() != null ?
                        diagnosis.getType().getArabicName() : null)
                .isPrimary(diagnosis.getIsPrimary())
                .notes(diagnosis.getNotes())
                .createdAt(diagnosis.getCreatedAt())
                .build();
    }
}
