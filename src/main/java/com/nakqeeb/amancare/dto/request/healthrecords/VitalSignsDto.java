package com.nakqeeb.amancare.dto.request.healthrecords;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSignsDto {

    @DecimalMin(value = "30.0", message = "درجة الحرارة يجب أن تكون أكبر من 30 درجة")
    @DecimalMax(value = "45.0", message = "درجة الحرارة يجب أن تكون أقل من 45 درجة")
    private BigDecimal temperature;

    @Min(value = 50, message = "الضغط الانقباضي يجب أن يكون أكبر من 50")
    @Max(value = 300, message = "الضغط الانقباضي يجب أن يكون أقل من 300")
    private Integer bloodPressureSystolic;

    @Min(value = 30, message = "الضغط الانبساطي يجب أن يكون أكبر من 30")
    @Max(value = 200, message = "الضغط الانبساطي يجب أن يكون أقل من 200")
    private Integer bloodPressureDiastolic;

    @Min(value = 40, message = "نبضات القلب يجب أن تكون أكبر من 40")
    @Max(value = 200, message = "نبضات القلب يجب أن تكون أقل من 200")
    private Integer heartRate;

    @Min(value = 8, message = "معدل التنفس يجب أن يكون أكبر من 8")
    @Max(value = 60, message = "معدل التنفس يجب أن يكون أقل من 60")
    private Integer respiratoryRate;

    @Min(value = 70, message = "تشبع الأكسجين يجب أن يكون أكبر من 70%")
    @Max(value = 100, message = "تشبع الأكسجين يجب أن يكون أقل من أو يساوي 100%")
    private Integer oxygenSaturation;

    @DecimalMin(value = "0.5", message = "الوزن يجب أن يكون أكبر من 0.5 كيلو")
    @DecimalMax(value = "500.0", message = "الوزن يجب أن يكون أقل من 500 كيلو")
    private BigDecimal weight;

    @DecimalMin(value = "30.0", message = "الطول يجب أن يكون أكبر من 30 سم")
    @DecimalMax(value = "300.0", message = "الطول يجب أن يكون أقل من 300 سم")
    private BigDecimal height;

    private BigDecimal bmi; // calculated

    @DecimalMin(value = "20.0", message = "مستوى السكر يجب أن يكون أكبر من 20")
    @DecimalMax(value = "800.0", message = "مستوى السكر يجب أن يكون أقل من 800")
    private BigDecimal bloodSugar;

    @Min(value = 0, message = "مقياس الألم يجب أن يكون بين 0 و 10")
    @Max(value = 10, message = "مقياس الألم يجب أن يكون بين 0 و 10")
    private Integer painScale;
}
