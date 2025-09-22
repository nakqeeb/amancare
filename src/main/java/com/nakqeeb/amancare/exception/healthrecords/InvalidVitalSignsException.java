package com.nakqeeb.amancare.exception.healthrecords;

import com.nakqeeb.amancare.exception.BusinessLogicException;

/**
 * استثناء العلامات الحيوية غير صالحة
 * Exception thrown when vital signs validation fails
 */
public class InvalidVitalSignsException extends BusinessLogicException {

    public InvalidVitalSignsException(String message) {
        super(message);
    }

    public static InvalidVitalSignsException temperatureOutOfRange(double temperature) {
        return new InvalidVitalSignsException("درجة حرارة خارج النطاق الطبيعي: " + temperature + "°C");
    }

    public static InvalidVitalSignsException bloodPressureOutOfRange(int systolic, int diastolic) {
        return new InvalidVitalSignsException("ضغط دم خارج النطاق الطبيعي: " + systolic + "/" + diastolic + " mmHg");
    }

    public static InvalidVitalSignsException heartRateOutOfRange(int heartRate) {
        return new InvalidVitalSignsException("نبضات قلب خارج النطاق الطبيعي: " + heartRate + " bpm");
    }

    public static InvalidVitalSignsException invalidBMI(double bmi) {
        return new InvalidVitalSignsException("مؤشر كتلة الجسم غير صالح: " + bmi);
    }
}
