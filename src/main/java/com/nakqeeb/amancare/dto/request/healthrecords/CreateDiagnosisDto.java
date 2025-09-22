package com.nakqeeb.amancare.dto.request.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.DiagnosisType;
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
public class CreateDiagnosisDto {

    private String icdCode;

    @NotBlank(message = "وصف التشخيص مطلوب")
    @Size(max = 500, message = "وصف التشخيص يجب أن يكون أقل من 500 حرف")
    private String description;

    @NotNull(message = "نوع التشخيص مطلوب")
    private DiagnosisType type;

    @NotNull(message = "تحديد التشخيص الأساسي مطلوب")
    private Boolean isPrimary;

    @Size(max = 500, message = "ملاحظات التشخيص يجب أن تكون أقل من 500 حرف")
    private String notes;
}
