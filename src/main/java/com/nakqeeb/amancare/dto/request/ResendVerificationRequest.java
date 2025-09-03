// ===================================================================
// 8. RESEND VERIFICATION DTO
// src/main/java/com/nakqeeb/amancare/dto/request/ResendVerificationRequest.java
// ===================================================================
package com.nakqeeb.amancare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendVerificationRequest {
    @NotBlank(message = "البريد الإلكتروني مطلوب")
    @Email(message = "البريد الإلكتروني غير صحيح")
    private String email;
}