package com.nakqeeb.amancare.dto.response.healthrecords;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisFrequencyResponse {

    private String diagnosis;
    private String icdCode;
    private Long count;
    private Double percentage;
}
