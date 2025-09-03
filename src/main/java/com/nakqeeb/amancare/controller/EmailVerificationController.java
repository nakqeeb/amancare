// ===================================================================
// 7. EMAIL VERIFICATION CONTROLLER
// src/main/java/com/nakqeeb/amancare/controller/EmailVerificationController.java
// ===================================================================
package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.dto.request.ResendVerificationRequest;
import com.nakqeeb.amancare.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "تأكيد البريد الإلكتروني", description = "APIs تأكيد البريد الإلكتروني")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * التحقق من صحة رمز تأكيد البريد الإلكتروني
     */
    @GetMapping("/validate-verification-token")
    @Operation(summary = "التحقق من رمز تأكيد البريد", description = "التحقق من صحة وصلاحية رمز تأكيد البريد الإلكتروني")
    public ResponseEntity<ApiResponse<Boolean>> validateVerificationToken(@RequestParam String token) {
        try {
            boolean isValid = emailVerificationService.validateVerificationToken(token);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            isValid ? "الرمز صحيح" : "الرمز غير صحيح أو منتهي الصلاحية",
                            isValid
                    )
            );
        } catch (Exception e) {
            log.error("خطأ في التحقق من رمز تأكيد البريد: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "خطأ في التحقق من الرمز", false)
            );
        }
    }

    /**
     * تأكيد البريد الإلكتروني وتفعيل الحساب
     */
    @PostMapping("/verify-email")
    @Operation(summary = "تأكيد البريد الإلكتروني", description = "تأكيد البريد الإلكتروني وتفعيل الحساب")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        try {
            emailVerificationService.verifyEmail(token);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "تم تفعيل حسابك بنجاح. يمكنك الآن تسجيل الدخول",
                            null
                    )
            );
        } catch (Exception e) {
            log.error("خطأ في تفعيل الحساب: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        }
    }

    /**
     * إعادة إرسال رابط تأكيد البريد الإلكتروني
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "إعادة إرسال رابط التأكيد", description = "إعادة إرسال رابط تأكيد البريد الإلكتروني")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(@Valid @RequestBody ResendVerificationRequest request) {
        try {
            emailVerificationService.resendVerificationEmail(request.getEmail());

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "تم إعادة إرسال رابط التأكيد إلى بريدك الإلكتروني",
                            null
                    )
            );
        } catch (Exception e) {
            log.error("خطأ في إعادة إرسال رابط التأكيد: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        }
    }
}