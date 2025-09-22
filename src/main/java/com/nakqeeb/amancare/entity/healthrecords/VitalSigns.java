// =============================================================================
// Medical Record Supporting Entities - كيانات السجلات الطبية المساعدة
// src/main/java/com/nakqeeb/amancare/entity/
// =============================================================================

package com.nakqeeb.amancare.entity.healthrecords;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

// =============================================================================
// VitalSigns Embeddable - العلامات الحيوية
// =============================================================================
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VitalSigns {

    @Column(name = "temperature")
    @DecimalMin(value = "30.0", message = "درجة الحرارة يجب أن تكون أكبر من 30 درجة")
    @DecimalMax(value = "45.0", message = "درجة الحرارة يجب أن تكون أقل من 45 درجة")
    private BigDecimal temperature; // Celsius

    @Column(name = "blood_pressure_systolic")
    @Min(value = 50, message = "الضغط الانقباضي يجب أن يكون أكبر من 50")
    @Max(value = 300, message = "الضغط الانقباضي يجب أن يكون أقل من 300")
    private Integer bloodPressureSystolic; // mmHg

    @Column(name = "blood_pressure_diastolic")
    @Min(value = 30, message = "الضغط الانبساطي يجب أن يكون أكبر من 30")
    @Max(value = 200, message = "الضغط الانبساطي يجب أن يكون أقل من 200")
    private Integer bloodPressureDiastolic; // mmHg

    @Column(name = "heart_rate")
    @Min(value = 40, message = "نبضات القلب يجب أن تكون أكبر من 40")
    @Max(value = 200, message = "نبضات القلب يجب أن تكون أقل من 200")
    private Integer heartRate; // bpm

    @Column(name = "respiratory_rate")
    @Min(value = 8, message = "معدل التنفس يجب أن يكون أكبر من 8")
    @Max(value = 60, message = "معدل التنفس يجب أن يكون أقل من 60")
    private Integer respiratoryRate; // breaths/min

    @Column(name = "oxygen_saturation")
    @Min(value = 70, message = "تشبع الأكسجين يجب أن يكون أكبر من 70%")
    @Max(value = 100, message = "تشبع الأكسجين يجب أن يكون أقل من أو يساوي 100%")
    private Integer oxygenSaturation; // %

    @Column(name = "weight")
    @DecimalMin(value = "0.5", message = "الوزن يجب أن يكون أكبر من 0.5 كيلو")
    @DecimalMax(value = "500.0", message = "الوزن يجب أن يكون أقل من 500 كيلو")
    private BigDecimal weight; // kg

    @Column(name = "height")
    @DecimalMin(value = "30.0", message = "الطول يجب أن يكون أكبر من 30 سم")
    @DecimalMax(value = "300.0", message = "الطول يجب أن يكون أقل من 300 سم")
    private BigDecimal height; // cm

    @Column(name = "bmi")
    private BigDecimal bmi; // calculated

    @Column(name = "blood_sugar")
    @DecimalMin(value = "20.0", message = "مستوى السكر يجب أن يكون أكبر من 20")
    @DecimalMax(value = "800.0", message = "مستوى السكر يجب أن يكون أقل من 800")
    private BigDecimal bloodSugar; // mg/dL

    @Column(name = "pain_scale")
    @Min(value = 0, message = "مقياس الألم يجب أن يكون بين 0 و 10")
    @Max(value = 10, message = "مقياس الألم يجب أن يكون بين 0 و 10")
    private Integer painScale; // 0-10
}

