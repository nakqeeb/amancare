package com.nakqeeb.amancare.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "الرمز مطلوب")
    private String token;

    @NotBlank(message = "كلمة المرور الجديدة مطلوبة")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "كلمة المرور يجب أن تحتوي على 8 أحرف على الأقل، حرف كبير وصغير، رقم، ورمز خاص"
    )
    private String newPassword;
}
