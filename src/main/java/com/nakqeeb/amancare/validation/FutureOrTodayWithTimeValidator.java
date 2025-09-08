package com.nakqeeb.amancare.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * محقق التاريخ والوقت للمواعيد
 * يسمح بحجز موعد في نفس اليوم إذا كان الوقت في المستقبل
 */
public class FutureOrTodayWithTimeValidator implements ConstraintValidator<FutureOrTodayWithTime, Object> {

    private String dateFieldName;
    private String timeFieldName;

    @Override
    public void initialize(FutureOrTodayWithTime constraintAnnotation) {
        this.dateFieldName = constraintAnnotation.dateField();
        this.timeFieldName = constraintAnnotation.timeField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        try {
            Field dateField = value.getClass().getDeclaredField(dateFieldName);
            Field timeField = value.getClass().getDeclaredField(timeFieldName);

            dateField.setAccessible(true);
            timeField.setAccessible(true);

            LocalDate appointmentDate = (LocalDate) dateField.get(value);
            LocalTime appointmentTime = (LocalTime) timeField.get(value);

            // If either date or time is null, let other validators handle it
            if (appointmentDate == null || appointmentTime == null) {
                return true;
            }

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            // Date is in the future - always valid
            if (appointmentDate.isAfter(today)) {
                return true;
            }

            // Date is in the past - always invalid
            if (appointmentDate.isBefore(today)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("تاريخ الموعد لا يمكن أن يكون في الماضي")
                        .addPropertyNode(dateFieldName)
                        .addConstraintViolation();
                return false;
            }

            // Date is today - check if time is in the future
            if (appointmentDate.isEqual(today)) {
                if (appointmentTime.isAfter(now)) {
                    return true;
                } else {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("وقت الموعد يجب أن يكون في المستقبل عند الحجز لنفس اليوم")
                            .addPropertyNode(timeFieldName)
                            .addConstraintViolation();
                    return false;
                }
            }

            return true;

        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Log error and return false if there's an issue accessing fields
            e.printStackTrace();
            return false;
        }
    }
}