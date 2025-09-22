package com.nakqeeb.amancare.dto.request.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.LabTestCategory;
import com.nakqeeb.amancare.entity.healthrecords.TestUrgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLabTestDto {

    @NotBlank(message = "اسم الفحص مطلوب")
    @Size(max = 200, message = "اسم الفحص يجب أن يكون أقل من 200 حرف")
    private String testName;

    @Size(max = 50, message = "كود الفحص يجب أن يكون أقل من 50 حرف")
    private String testCode;

    @NotNull(message = "فئة الفحص مطلوبة")
    private LabTestCategory category;

    @NotNull(message = "أولوية الفحص مطلوبة")
    private TestUrgency urgency;

    @Size(max = 100, message = "نوع العينة يجب أن يكون أقل من 100 حرف")
    private String specimenType;

    @Size(max = 500, message = "تعليمات الفحص يجب أن تكون أقل من 500 حرف")
    private String instructions;
}
