package com.nakqeeb.amancare.dto.request.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.RadiologyType;
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
public class CreateRadiologyTestDto {

    @NotBlank(message = "اسم فحص الأشعة مطلوب")
    @Size(max = 200, message = "اسم فحص الأشعة يجب أن يكون أقل من 200 حرف")
    private String testName;

    @NotNull(message = "نوع الأشعة مطلوب")
    private RadiologyType testType;

    @Size(max = 100, message = "الجزء المراد فحصه يجب أن يكون أقل من 100 حرف")
    private String bodyPart;

    @NotNull(message = "أولوية الفحص مطلوبة")
    private TestUrgency urgency;

    @Size(max = 500, message = "تعليمات الفحص يجب أن تكون أقل من 500 حرف")
    private String instructions;
}
