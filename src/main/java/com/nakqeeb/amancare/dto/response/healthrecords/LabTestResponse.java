package com.nakqeeb.amancare.dto.response.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.LabTest;
import com.nakqeeb.amancare.entity.healthrecords.LabTestCategory;
import com.nakqeeb.amancare.entity.healthrecords.TestStatus;
import com.nakqeeb.amancare.entity.healthrecords.TestUrgency;
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
public class LabTestResponse {

    private Long id;
    private String testName;
    private String testCode;
    private LabTestCategory category;
    private String categoryArabic;
    private TestUrgency urgency;
    private String urgencyArabic;
    private String specimenType;
    private String instructions;
    private TestStatus status;
    private String statusArabic;
    private LocalDate orderedDate;
    private LocalDate resultDate;
    private String results;
    private String normalRange;
    private String interpretation;
    private String performedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LabTestResponse fromEntity(LabTest labTest) {
        return LabTestResponse.builder()
                .id(labTest.getId())
                .testName(labTest.getTestName())
                .testCode(labTest.getTestCode())
                .category(labTest.getCategory())
                .categoryArabic(labTest.getCategory() != null ?
                        labTest.getCategory().getArabicName() : null)
                .urgency(labTest.getUrgency())
                .urgencyArabic(labTest.getUrgency() != null ?
                        labTest.getUrgency().getArabicName() : null)
                .specimenType(labTest.getSpecimenType())
                .instructions(labTest.getInstructions())
                .status(labTest.getStatus())
                .statusArabic(labTest.getStatus() != null ?
                        labTest.getStatus().getArabicName() : null)
                .orderedDate(labTest.getOrderedDate())
                .resultDate(labTest.getResultDate())
                .results(labTest.getResults())
                .normalRange(labTest.getNormalRange())
                .interpretation(labTest.getInterpretation())
                .performedBy(labTest.getPerformedBy())
                .createdAt(labTest.getCreatedAt())
                .updatedAt(labTest.getUpdatedAt())
                .build();
    }
}
