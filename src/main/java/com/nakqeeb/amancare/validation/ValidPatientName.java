package com.nakqeeb.amancare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * التحقق من صحة الأسماء العربية والإنجليزية
 */
@Documented
@Constraint(validatedBy = PatientNameValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPatientName {
    String message() default "الاسم يجب أن يحتوي على أحرف عربية أو إنجليزية فقط";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}