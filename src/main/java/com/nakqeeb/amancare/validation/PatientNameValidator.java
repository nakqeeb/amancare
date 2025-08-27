package com.nakqeeb.amancare.validation;

import com.nakqeeb.amancare.util.ValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * محقق أسماء المرضى
 */
public class PatientNameValidator implements ConstraintValidator<ValidPatientName, String> {

    @Override
    public void initialize(ValidPatientName constraintAnnotation) {
        // لا حاجة للتهيئة
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return ValidationUtil.isValidName(name);
    }
}
