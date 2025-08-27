// =============================================================================
// Auth Controller - وحدة التحكم بالمصادقة
// =============================================================================

package com.nakqeeb.amancare.controller;

import com.nakqeeb.amancare.dto.request.LoginRequest;
import com.nakqeeb.amancare.dto.request.ClinicRegistrationRequest;
import com.nakqeeb.amancare.dto.request.UserCreationRequest;
import com.nakqeeb.amancare.dto.request.ChangePasswordRequest;
import com.nakqeeb.amancare.dto.request.RefreshTokenRequest;
import com.nakqeeb.amancare.dto.response.JwtAuthenticationResponse;
import com.nakqeeb.amancare.dto.response.ApiResponse;
import com.nakqeeb.amancare.dto.response.UserResponse;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.exception.ForbiddenOperationException;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * وحدة التحكم بالمصادقة والتفويض
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "المصادقة", description = "APIs الخاصة بالمصادقة وإدارة الحسابات")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * تسجيل الدخول
     */
    @PostMapping("/login")
    @Operation(summary = "تسجيل الدخول", description = "تسجيل الدخول للنظام باستخدام اسم المستخدم/البريد الإلكتروني وكلمة المرور")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtAuthenticationResponse jwtResponse = authService.login(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تسجيل الدخول بنجاح", jwtResponse)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "بيانات تسجيل الدخول غير صحيحة: " + e.getMessage(), null));
        }
    }

    /**
     * تسجيل عيادة جديدة مع مدير العيادة
     */
    @PostMapping("/register-clinic")
    @Operation(summary = "تسجيل عيادة جديدة", description = "تسجيل عيادة جديدة مع إنشاء حساب مدير العيادة")
    public ResponseEntity<ApiResponse<UserResponse>> registerClinic(@Valid @RequestBody ClinicRegistrationRequest request) {
        try {
            User admin = authService.registerClinicWithAdmin(request);
            UserResponse userResponse = UserResponse.fromUser(admin);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "تم تسجيل العيادة وإنشاء حساب المدير بنجاح", userResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في تسجيل العيادة: " + e.getMessage(), null));
        }
    }

    /**
     * إنشاء مستخدم جديد في العيادة
     */
    @PostMapping("/create-user")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN')")
    @Operation(
            summary = "👥 إنشاء مستخدم جديد",
            description = """
            إنشاء مستخدم جديد في العيادة مع قيود أمنية:
            
            **مدير النظام (SYSTEM_ADMIN):**
            - يمكنه إنشاء أي دور في أي عيادة
            
            **مدير العيادة (ADMIN):**
            - يمكنه إنشاء: طبيب، ممرض/ممرضة، موظف استقبال فقط
            - في عيادته فقط
            
            **باقي الأدوار:** لا يمكنهم إنشاء مستخدمين
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "إنشاء طبيب",
                                            value = """
                            {
                              "username": "doctor1",
                              "email": "doctor@clinic.com",
                              "password": "password123",
                              "firstName": "أحمد",
                              "lastName": "محمد",
                              "phone": "773111222",
                              "role": "DOCTOR",
                              "specialization": "طب عام"
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "إنشاء ممرض/ممرضة",
                                            value = """
                            {
                              "username": "nurse1",
                              "email": "nurse@clinic.com",
                              "password": "password123",
                              "firstName": "فاطمة",
                              "lastName": "أحمد",
                              "phone": "773222333",
                              "role": "NURSE"
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "إنشاء موظف استقبال",
                                            value = """
                            {
                              "username": "receptionist1",
                              "email": "receptionist@clinic.com",
                              "password": "password123",
                              "firstName": "علي",
                              "lastName": "محمد",
                              "phone": "773333444",
                              "role": "RECEPTIONIST"
                            }
                            """
                                    )
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "تم إنشاء المستخدم بنجاح"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "بيانات غير صحيحة"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ممنوع - لا تملك صلاحية إنشاء هذا الدور"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "اسم المستخدم أو البريد الإلكتروني موجود بالفعل")
    })
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UserCreationRequest request) {
        try {
            User newUser = authService.createUser(currentUser.getClinicId(), request, currentUser);
            UserResponse userResponse = UserResponse.fromUser(newUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "تم إنشاء المستخدم بنجاح", userResponse));
        } catch (ForbiddenOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في إنشاء المستخدم: " + e.getMessage(), null));
        }
    }

    /**
     * تغيير كلمة المرور
     */
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(summary = "تغيير كلمة المرور", description = "تغيير كلمة المرور للمستخدم الحالي")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(currentUser.getId(), request.getOldPassword(), request.getNewPassword());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تغيير كلمة المرور بنجاح", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "فشل في تغيير كلمة المرور: " + e.getMessage(), null));
        }
    }

    /**
     * تحديث الرمز المميز
     */
    @PostMapping("/refresh")
    @Operation(summary = "تحديث الرمز المميز", description = "تحديث access token باستخدام refresh token")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            JwtAuthenticationResponse jwtResponse = authService.refreshToken(request.getRefreshToken());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم تحديث الرمز المميز بنجاح", jwtResponse)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "فشل في تحديث الرمز المميز: " + e.getMessage(), null));
        }
    }

    /**
     * معلومات المستخدم الحالي
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(summary = "معلومات المستخدم الحالي", description = "الحصول على معلومات المستخدم المُسجل حالياً")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            UserResponse userResponse = UserResponse.fromUserPrincipal(currentUser);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "تم الحصول على معلومات المستخدم بنجاح", userResponse)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "فشل في الحصول على معلومات المستخدم: " + e.getMessage(), null));
        }
    }

    /**
     * تسجيل الخروج (إبطال الرمز المميز)
     */
    @PostMapping("/logout")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(summary = "تسجيل الخروج", description = "تسجيل الخروج من النظام")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal currentUser) {
        // في التطبيقات الحقيقية، يمكن إضافة الرمز المميز لقائمة سوداء
        // لكن في هذا التطبيق البسيط، سنعتمد على انتهاء صلاحية الرمز من جانب العميل

        return ResponseEntity.ok(
                new ApiResponse<>(true, "تم تسجيل الخروج بنجاح", null)
        );
    }

    /**
     * فحص صحة الرمز المميز
     */
    @GetMapping("/validate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('RECEPTIONIST')")
    @Operation(summary = "فحص صحة الرمز المميز", description = "التحقق من صحة الرمز المميز الحالي")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "الرمز المميز صحيح وفعال", true)
        );
    }
}
