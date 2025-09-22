package com.nakqeeb.amancare.dto.response.healthrecords;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationFrequencyResponse {

    private String medicationName;
    private String genericName;
    private Long count;
    private Double percentage;
}
