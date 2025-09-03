// ===================================================================
// 6. PASSWORD RESET CONTROLLER
// ===================================================================
package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.dto.request.PasswordResetRequest;
import com.nakqeeb.amancare.dto.request.ResetPasswordRequest;
import com.nakqeeb.amancare.service.PasswordResetService;
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
@Tag(name = "إعادة تعيين كلمة المرور", description = "APIs إعادة تعيين كلمة المرور")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * طلب إعادة تعيين كلمة المرور (إرسال البريد الإلكتروني)
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "طلب إعادة تعيين كلمة المرور", description = "إرسال رابط إعادة تعيين كلمة المرور إلى البريد الإلكتروني")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            passwordResetService.initiatePasswordReset(request.getEmail());

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "تم إرسال رابط إعادة تعيين كلمة المرور إلى بريدك الإلكتروني",
                            null
                    )
            );
        } catch (Exception e) {
            log.error("خطأ في طلب إعادة تعيين كلمة المرور: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "حدث خطأ أثناء معالجة الطلب", null)
            );
        }
    }

    /**
     * التحقق من صحة رمز إعادة التعيين
     */
    @GetMapping("/validate-reset-token")
    @Operation(summary = "التحقق من رمز إعادة التعيين", description = "التحقق من صحة وصلاحية رمز إعادة تعيين كلمة المرور")
    public ResponseEntity<ApiResponse<Boolean>> validateResetToken(@RequestParam String token) {
        try {
            boolean isValid = passwordResetService.validateResetToken(token);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            isValid ? "الرمز صحيح" : "الرمز غير صحيح أو منتهي الصلاحية",
                            isValid
                    )
            );
        } catch (Exception e) {
            log.error("خطأ في التحقق من رمز إعادة التعيين: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "خطأ في التحقق من الرمز", false)
            );
        }
    }

    /**
     * إعادة تعيين كلمة المرور
     */
    @PostMapping("/reset-password")
    @Operation(summary = "إعادة تعيين كلمة المرور", description = "إعادة تعيين كلمة المرور باستخدام الرمز الصحيح")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "تم تغيير كلمة المرور بنجاح",
                            null
                    )
            );
        } catch (Exception e) {
            log.error("خطأ في إعادة تعيين كلمة المرور: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null)
            );
        }
    }
}
