package com.nakqeeb.amancare.dto.response.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.RadiologyTest;
import com.nakqeeb.amancare.entity.healthrecords.RadiologyType;
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
public class RadiologyTestResponse {

    private Long id;
    private String testName;
    private RadiologyType testType;
    private String testTypeArabic;
    private String bodyPart;
    private TestUrgency urgency;
    private String urgencyArabic;
    private String instructions;
    private TestStatus status;
    private String statusArabic;
    private LocalDate orderedDate;
    private LocalDate performedDate;
    private String findings;
    private String impression;
    private String radiologistName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RadiologyTestResponse fromEntity(RadiologyTest radiologyTest) {
        return RadiologyTestResponse.builder()
                .id(radiologyTest.getId())
                .testName(radiologyTest.getTestName())
                .testType(radiologyTest.getTestType())
                .testTypeArabic(radiologyTest.getTestType() != null ?
                        radiologyTest.getTestType().getArabicName() : null)
                .bodyPart(radiologyTest.getBodyPart())
                .urgency(radiologyTest.getUrgency())
                .urgencyArabic(radiologyTest.getUrgency() != null ?
                        radiologyTest.getUrgency().getArabicName() : null)
                .instructions(radiologyTest.getInstructions())
                .status(radiologyTest.getStatus())
                .statusArabic(radiologyTest.getStatus() != null ?
                        radiologyTest.getStatus().getArabicName() : null)
                .orderedDate(radiologyTest.getOrderedDate())
                .performedDate(radiologyTest.getPerformedDate())
                .findings(radiologyTest.getFindings())
                .impression(radiologyTest.getImpression())
                .radiologistName(radiologyTest.getRadiologistName())
                .createdAt(radiologyTest.getCreatedAt())
                .updatedAt(radiologyTest.getUpdatedAt())
                .build();
    }
}
