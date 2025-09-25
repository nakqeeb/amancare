// =============================================================================
// Global Exception Handler - معالج الاستثناءات العام
// =============================================================================

package com.nakqeeb.amancare.exception;

import com.nakqeeb.amancare.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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


    /**
     * Handle enum conversion errors with helpful messages
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String parameterName = ex.getName();
        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String requiredType = ex.getRequiredType() != null ?
                ex.getRequiredType().getSimpleName() : "unknown";

        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("parameter", parameterName);
        errorDetails.put("invalidValue", invalidValue);
        errorDetails.put("expectedType", requiredType);

        String message;
        String hint = "";

        // Provide specific guidance based on the parameter type
        if (requiredType.equals("BloodType")) {
            message = String.format(
                    "قيمة غير صحيحة لفصيلة الدم: '%s'",
                    invalidValue
            );
            hint = "القيم الصحيحة: O+, O-, A+, A-, B+, B-, AB+, AB- أو O_POSITIVE, O_NEGATIVE, A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE, AB_POSITIVE, AB_NEGATIVE";
            errorDetails.put("validValues", "O+, O-, A+, A-, B+, B-, AB+, AB-");
            errorDetails.put("alternativeFormat", "O_POSITIVE, O_NEGATIVE, etc.");
        } else if (requiredType.equals("Gender")) {
            message = String.format(
                    "قيمة غير صحيحة للجنس: '%s'",
                    invalidValue
            );
            hint = "القيم الصحيحة: MALE, FEMALE, M, F, ذكر, أنثى";
            errorDetails.put("validValues", "MALE, FEMALE, M, F, ذكر, أنثى");
        } else {
            message = String.format(
                    "قيمة غير صحيحة للمعامل '%s': '%s'. النوع المتوقع: %s",
                    parameterName, invalidValue, requiredType
            );
        }

        if (!hint.isEmpty()) {
            errorDetails.put("hint", hint);
            message = message + ". " + hint;
        }

        logger.warn("Type mismatch error: parameter={}, value={}, type={}",
                parameterName, invalidValue, requiredType);

        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                false,
                message,
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle general type mismatch exceptions
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleGeneralTypeMismatch(
            TypeMismatchException ex) {

        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String requiredType = ex.getRequiredType() != null ?
                ex.getRequiredType().getSimpleName() : "unknown";

        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("invalidValue", invalidValue);
        errorDetails.put("expectedType", requiredType);
        errorDetails.put("error", ex.getMessage());

        String message = String.format(
                "خطأ في تحويل القيمة: '%s' إلى النوع المطلوب: %s",
                invalidValue, requiredType
        );

        logger.warn("Type mismatch: value={}, type={}", invalidValue, requiredType, ex);

        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                false,
                message,
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle missing required parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMissingParams(
            MissingServletRequestParameterException ex) {

        String parameterName = ex.getParameterName();
        String parameterType = ex.getParameterType();

        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("missingParameter", parameterName);
        errorDetails.put("parameterType", parameterType);

        String message = String.format(
                "المعامل المطلوب '%s' غير موجود",
                parameterName
        );

        logger.warn("Missing required parameter: {}", parameterName);

        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                false,
                message,
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle IllegalArgumentException (from custom converters)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgument(
            IllegalArgumentException ex) {

        String message = ex.getMessage();

        // Check if it's an enum conversion error
        if (message != null && (
                message.contains("Invalid blood type") ||
                        message.contains("Invalid gender") ||
                        message.contains("Cannot parse"))) {

            logger.warn("Enum conversion error: {}", message);

            ApiResponse<String> response = new ApiResponse<>(
                    false,
                    message,
                    null
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // For other IllegalArgumentExceptions
        logger.error("IllegalArgumentException: ", ex);

        ApiResponse<String> response = new ApiResponse<>(
                false,
                "خطأ في المعاملات: " + message,
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle validation exceptions if using Bean Validation
     */
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(
            org.springframework.validation.BindException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        ApiResponse<Map<String, String>> response = new ApiResponse<>(
                false,
                "خطأ في التحقق من البيانات",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle DuplicateResourceException
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {

        logger.error("Duplicate resource: {}", ex.getMessage());

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setData(createErrorDetails(ex, request));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle ConflictException
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflictException(
            ConflictException ex, WebRequest request) {

        logger.error("Conflict: {}", ex.getMessage());

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setData(createErrorDetails(ex, request));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle ForbiddenOperationException
     */
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleForbiddenOperationException(
            ForbiddenOperationException ex, WebRequest request) {

        logger.error("Forbidden operation: {}", ex.getMessage());

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setData(createErrorDetails(ex, request));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * معالجة استثناءات عدم وجود صلاحية
     * Handle unauthorized exceptions
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {

        Map<String, Object> errorData = new HashMap<>();
        errorData.put("timestamp", LocalDateTime.now());
        errorData.put("path", request.getDescription(false).replace("uri=", ""));

        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setData(errorData);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}