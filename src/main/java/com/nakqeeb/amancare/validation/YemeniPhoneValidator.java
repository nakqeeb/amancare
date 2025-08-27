package com.nakqeeb.amancare.validation;


import com.nakqeeb.amancare.util.ValidationUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * محقق أرقام الهواتف اليمنية
 */
public class YemeniPhoneValidator implements ConstraintValidator<ValidYemeniPhone, String> {

    @Override
    public void initialize(ValidYemeniPhone constraintAnnotation) {
        // لا حاجة للتهيئة
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        return ValidationUtil.isValidYemeniPhone(phone);
    }
}