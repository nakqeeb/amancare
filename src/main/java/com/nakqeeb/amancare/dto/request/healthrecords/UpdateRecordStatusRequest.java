package com.nakqeeb.amancare.dto.request.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.RecordStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecordStatusRequest {

    @NotNull(message = "حالة السجل مطلوبة")
    private RecordStatus status;

    @Size(max = 500, message = "ملاحظات تغيير الحالة يجب أن تكون أقل من 500 حرف")
    private String notes;
}
