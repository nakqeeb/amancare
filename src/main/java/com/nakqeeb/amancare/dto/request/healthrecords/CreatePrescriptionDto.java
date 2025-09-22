package com.nakqeeb.amancare.dto.request.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.MedicationRoute;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePrescriptionDto {

    @NotBlank(message = "اسم الدواء مطلوب")
    @Size(max = 200, message = "اسم الدواء يجب أن يكون أقل من 200 حرف")
    private String medicationName;

    @Size(max = 200, message = "الاسم العلمي يجب أن يكون أقل من 200 حرف")
    private String genericName;

    @NotBlank(message = "الجرعة مطلوبة")
    @Size(max = 100, message = "الجرعة يجب أن تكون أقل من 100 حرف")
    private String dosage;

    @NotBlank(message = "عدد مرات الاستخدام مطلوب")
    @Size(max = 100, message = "عدد مرات الاستخدام يجب أن يكون أقل من 100 حرف")
    private String frequency;

    @NotBlank(message = "مدة العلاج مطلوبة")
    @Size(max = 100, message = "مدة العلاج يجب أن تكون أقل من 100 حرف")
    private String duration;

    @NotNull(message = "طريقة إعطاء الدواء مطلوبة")
    private MedicationRoute route;

    @Size(max = 500, message = "تعليمات الاستخدام يجب أن تكون أقل من 500 حرف")
    private String instructions;

    @Min(value = 1, message = "الكمية يجب أن تكون أكبر من 0")
    private Integer quantity;

    @Min(value = 0, message = "عدد التجديدات يجب أن يكون 0 أو أكثر")
    @Max(value = 12, message = "عدد التجديدات يجب أن يكون أقل من أو يساوي 12")
    private Integer refills;

    private LocalDate startDate;

    private LocalDate endDate;

    @Builder.Default
    private Boolean isPrn = false;
}
