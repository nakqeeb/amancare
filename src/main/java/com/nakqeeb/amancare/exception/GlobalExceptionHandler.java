// =============================================================================
// Global Exception Handler - معالج الاستثناءات العام
// =============================================================================

package com.nakqeeb.amancare.exception;

import com.nakqeeb.amancare.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * معالج الاستثناءات العام للتطبيق
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * معالجة استثناء المورد غير الموجود
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setData(createErrorDetails(ex, request));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * معالجة استثناء الطلب غير الصحيح
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(
            BadRequestException ex, WebRequest request) {

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setData(createErrorDetails(ex, request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * معالجة أخطاء التحقق من البيانات
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("بيانات الإدخال غير صحيحة");
        response.setData(Map.of(
                "validationErrors", validationErrors,
                "timestamp", LocalDateTime.now(),
                "path", request.getDescription(false).replace("uri=", "")
        ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * معالجة استثناءات المصادقة
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            Exception ex, WebRequest request) {

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("بيانات تسجيل الدخول غير صحيحة");
        response.setData(createErrorDetails(ex, request));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * معالجة استثناءات التفويض
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("ليس لديك صلاحية للوصول إلى هذا المورد");
        response.setData(createErrorDetails(ex, request));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * معالجة استثناءات قاعدة البيانات
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException ex, WebRequest request) {

        String message = "خطأ في البيانات - قد تكون هناك بيانات مكررة أو مخالفة للقواعد";

        // تخصيص الرسالة حسب نوع القيد المخالف
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("Duplicate entry")) {
                message = "البيانات موجودة بالفعل - يرجى التحقق من البيانات المدخلة";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "لا يمكن حذف هذا العنصر لأنه مرتبط ببيانات أخرى";
            }
        }

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(createErrorDetails(ex, request));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * معالجة جميع الاستثناءات الأخرى
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("حدث خطأ في الخادم - يرجى المحاولة مرة أخرى");
        response.setData(createErrorDetails(ex, request));

        // تسجيل الخطأ للمطورين
        System.err.println("Unhandled exception: " + ex.getClass().getName() + " - " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * إنشاء تفاصيل الخطأ
     */
    private Map<String, Object> createErrorDetails(Exception ex, WebRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now());
        details.put("path", request.getDescription(false).replace("uri=", ""));
        details.put("error", ex.getClass().getSimpleName());

        // إضافة تفاصيل إضافية في بيئة التطوير فقط
        String activeProfile = System.getProperty("spring.profiles.active", "");
        if ("dev".equals(activeProfile)) {
            details.put("message", ex.getMessage());
            details.put("cause", ex.getCause() != null ? ex.getCause().toString() : null);
        }

        return details;
    }
}