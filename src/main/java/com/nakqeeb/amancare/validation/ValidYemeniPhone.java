package com.nakqeeb.amancare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
/**
 * التحقق من صحة أرقام الهواتف اليمنية
 */
@Documented
@Constraint(validatedBy = YemeniPhoneValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidYemeniPhone {
    String message() default "رقم الهاتف يجب أن يكون رقماً يمنياً صحيحاً";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}