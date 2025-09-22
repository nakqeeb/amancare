package com.nakqeeb.amancare.dto.response.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.VitalSigns;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSignsResponse {

    private BigDecimal temperature;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Integer heartRate;
    private Integer respiratoryRate;
    private Integer oxygenSaturation;
    private BigDecimal weight;
    private BigDecimal height;
    private BigDecimal bmi;
    private BigDecimal bloodSugar;
    private Integer painScale;

    public static VitalSignsResponse fromEmbeddable(VitalSigns vitalSigns) {
        return VitalSignsResponse.builder()
                .temperature(vitalSigns.getTemperature())
                .bloodPressureSystolic(vitalSigns.getBloodPressureSystolic())
                .bloodPressureDiastolic(vitalSigns.getBloodPressureDiastolic())
                .heartRate(vitalSigns.getHeartRate())
                .respiratoryRate(vitalSigns.getRespiratoryRate())
                .oxygenSaturation(vitalSigns.getOxygenSaturation())
                .weight(vitalSigns.getWeight())
                .height(vitalSigns.getHeight())
                .bmi(vitalSigns.getBmi())
                .bloodSugar(vitalSigns.getBloodSugar())
                .painScale(vitalSigns.getPainScale())
                .build();
    }
}
