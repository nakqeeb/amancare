package com.nakqeeb.amancare.dto.request.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.ProcedureCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMedicalProcedureDto {

    @NotBlank(message = "اسم الإجراء مطلوب")
    @Size(max = 200, message = "اسم الإجراء يجب أن يكون أقل من 200 حرف")
    private String procedureName;

    @Size(max = 50, message = "كود الإجراء يجب أن يكون أقل من 50 حرف")
    private String procedureCode;

    @NotNull(message = "فئة الإجراء مطلوبة")
    private ProcedureCategory category;

    private String description;

    @NotNull(message = "تاريخ تنفيذ الإجراء مطلوب")
    private LocalDate performedDate;

    @Size(max = 100, message = "اسم من قام بالإجراء يجب أن يكون أقل من 100 حرف")
    private String performedBy;

    private String complications;

    private String outcome;

    private String notes;
}
