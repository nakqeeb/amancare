package com.nakqeeb.amancare.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * التحقق من أن التاريخ في المستقبل أو اليوم
 * يسمح بتاريخ اليوم إذا كان الوقت في المستقبل
 */
@Documented
@Constraint(validatedBy = FutureOrTodayWithTimeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureOrTodayWithTime {
    String message() default "تاريخ الموعد يجب أن يكون في المستقبل أو اليوم مع وقت مستقبلي";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String dateField() default "appointmentDate";
    String timeField() default "appointmentTime";
}