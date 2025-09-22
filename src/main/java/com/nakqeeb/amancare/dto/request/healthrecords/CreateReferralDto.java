package com.nakqeeb.amancare.dto.request.healthrecords;

import com.nakqeeb.amancare.entity.healthrecords.ReferralPriority;
import com.nakqeeb.amancare.entity.healthrecords.ReferralType;
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
public class CreateReferralDto {

    @NotNull(message = "نوع التحويل مطلوب")
    private ReferralType referralType;

    @NotBlank(message = "المحول إليه مطلوب")
    @Size(max = 200, message = "المحول إليه يجب أن يكون أقل من 200 حرف")
    private String referredTo;

    @Size(max = 100, message = "التخصص يجب أن يكون أقل من 100 حرف")
    private String specialty;

    @NotNull(message = "أولوية التحويل مطلوبة")
    private ReferralPriority priority;

    @NotBlank(message = "سبب التحويل مطلوب")
    private String reason;

    private String notes;

    @NotNull(message = "تاريخ التحويل مطلوب")
    private LocalDate referralDate;

    private LocalDate appointmentDate;
}
