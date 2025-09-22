// =============================================================================
// Medical Records Utility Classes
// =============================================================================

package com.nakqeeb.amancare.util;

import com.nakqeeb.amancare.entity.healthrecords.*;
import com.nakqeeb.amancare.entity.*;
import com.nakqeeb.amancare.exception.healthrecords.InvalidDiagnosisException;
import com.nakqeeb.amancare.exception.healthrecords.InvalidVitalSignsException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.regex.Pattern;

/**
 * أدوات السجلات الطبية
 * Utility class for medical records operations
 */
public class MedicalRecordUtils {

    // Constants for validation ranges
    public static final double MIN_TEMPERATURE = 30.0;
    public static final double MAX_TEMPERATURE = 45.0;
    public static final int MIN_SYSTOLIC_BP = 50;
    public static final int MAX_SYSTOLIC_BP = 300;
    public static final int MIN_DIASTOLIC_BP = 30;
    public static final int MAX_DIASTOLIC_BP = 200;
    public static final int MIN_HEART_RATE = 40;
    public static final int MAX_HEART_RATE = 200;
    public static final int MIN_RESPIRATORY_RATE = 8;
    public static final int MAX_RESPIRATORY_RATE = 60;
    public static final int MIN_OXYGEN_SATURATION = 70;
    public static final int MAX_OXYGEN_SATURATION = 100;
    public static final double MIN_WEIGHT = 0.5;
    public static final double MAX_WEIGHT = 500.0;
    public static final double MIN_HEIGHT = 30.0;
    public static final double MAX_HEIGHT = 300.0;

    // BMI Categories (WHO Classification)
    public static final double BMI_UNDERWEIGHT = 18.5;
    public static final double BMI_NORMAL_UPPER = 24.9;
    public static final double BMI_OVERWEIGHT_UPPER = 29.9;
    public static final double BMI_OBESE_CLASS1_UPPER = 34.9;
    public static final double BMI_OBESE_CLASS2_UPPER = 39.9;

    // ICD-10 Code Pattern
    private static final Pattern ICD10_PATTERN = Pattern.compile("^[A-Z]\\d{2}(\\.\\d{1,2})?$");

    /**
     * حساب مؤشر كتلة الجسم
     * Calculate Body Mass Index (BMI)
     */
    public static BigDecimal calculateBMI(BigDecimal weight, BigDecimal height) {
        if (weight == null || height == null) {
            return null;
        }

        if (weight.compareTo(BigDecimal.ZERO) <= 0 || height.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("الوزن والطول يجب أن يكونا أكبر من الصفر");
        }

        // Convert height from cm to meters
        BigDecimal heightInMeters = height.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        // BMI = weight (kg) / height (m)^2
        BigDecimal heightSquared = heightInMeters.multiply(heightInMeters);
        return weight.divide(heightSquared, 2, RoundingMode.HALF_UP);
    }

    /**
     * تصنيف مؤشر كتلة الجسم
     * Classify BMI category
     */
    public static String classifyBMI(BigDecimal bmi) {
        if (bmi == null) {
            return "غير محدد";
        }

        double bmiValue = bmi.doubleValue();

        if (bmiValue < BMI_UNDERWEIGHT) {
            return "نقص في الوزن";
        } else if (bmiValue <= BMI_NORMAL_UPPER) {
            return "وزن طبيعي";
        } else if (bmiValue <= BMI_OVERWEIGHT_UPPER) {
            return "زيادة في الوزن";
        } else if (bmiValue <= BMI_OBESE_CLASS1_UPPER) {
            return "سمنة من الدرجة الأولى";
        } else if (bmiValue <= BMI_OBESE_CLASS2_UPPER) {
            return "سمنة من الدرجة الثانية";
        } else {
            return "سمنة مفرطة";
        }
    }

    /**
     * التحقق من صحة العلامات الحيوية
     * Validate vital signs values
     */
    public static void validateVitalSigns(VitalSigns vitalSigns) {
        if (vitalSigns == null) {
            return;
        }

        // Validate temperature
        if (vitalSigns.getTemperature() != null) {
            double temp = vitalSigns.getTemperature().doubleValue();
            if (temp < MIN_TEMPERATURE || temp > MAX_TEMPERATURE) {
                throw InvalidVitalSignsException.temperatureOutOfRange(temp);
            }
        }

        // Validate blood pressure
        if (vitalSigns.getBloodPressureSystolic() != null) {
            int systolic = vitalSigns.getBloodPressureSystolic();
            if (systolic < MIN_SYSTOLIC_BP || systolic > MAX_SYSTOLIC_BP) {
                throw InvalidVitalSignsException.bloodPressureOutOfRange(systolic,
                        vitalSigns.getBloodPressureDiastolic() != null ? vitalSigns.getBloodPressureDiastolic() : 0);
            }
        }

        if (vitalSigns.getBloodPressureDiastolic() != null) {
            int diastolic = vitalSigns.getBloodPressureDiastolic();
            if (diastolic < MIN_DIASTOLIC_BP || diastolic > MAX_DIASTOLIC_BP) {
                throw InvalidVitalSignsException.bloodPressureOutOfRange(
                        vitalSigns.getBloodPressureSystolic() != null ? vitalSigns.getBloodPressureSystolic() : 0,
                        diastolic);
            }
        }

        // Validate heart rate
        if (vitalSigns.getHeartRate() != null) {
            int hr = vitalSigns.getHeartRate();
            if (hr < MIN_HEART_RATE || hr > MAX_HEART_RATE) {
                throw InvalidVitalSignsException.heartRateOutOfRange(hr);
            }
        }

        // Validate other vital signs...
        // Additional validations can be added here

        // Calculate BMI if weight and height are provided
        if (vitalSigns.getWeight() != null && vitalSigns.getHeight() != null) {
            BigDecimal bmi = calculateBMI(vitalSigns.getWeight(), vitalSigns.getHeight());
            vitalSigns.setBmi(bmi);
        }
    }

    /**
     * التحقق من صحة التشخيص
     * Validate diagnosis list
     */
    public static void validateDiagnoses(List<Diagnosis> diagnoses) {
        if (diagnoses == null || diagnoses.isEmpty()) {
            throw InvalidDiagnosisException.emptyDiagnosis();
        }

        long primaryCount = diagnoses.stream()
                .filter(d -> d.getIsPrimary())
                .count();

        if (primaryCount == 0) {
            throw InvalidDiagnosisException.noPrimaryDiagnosis();
        }

        if (primaryCount > 1) {
            throw InvalidDiagnosisException.multiplePrimaryDiagnoses();
        }
    }

    /**
     * التحقق من صحة كود ICD-10
     * Validate ICD-10 code format
     */
    public static boolean isValidICD10Code(String code) {
        if (code == null || code.trim().isEmpty()) {
            return true; // ICD code is optional
        }
        return ICD10_PATTERN.matcher(code.trim().toUpperCase()).matches();
    }

    /**
     * تنسيق كود ICD-10
     * Format ICD-10 code
     */
    public static String formatICD10Code(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return code.trim().toUpperCase();
    }

    /**
     * حساب العمر من تاريخ الميلاد
     * Calculate age from birth date
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * تحديد الفئة العمرية
     * Determine age group
     */
    public static String getAgeGroup(int age) {
        if (age < 2) return "رضيع";
        if (age < 12) return "طفل";
        if (age < 18) return "مراهق";
        if (age < 65) return "بالغ";
        return "مسن";
    }

    /**
     * تقييم حالة الطوارئ بناء على العلامات الحيوية
     * Assess emergency status based on vital signs
     */
    public static boolean isEmergencyCase(VitalSigns vitalSigns) {
        if (vitalSigns == null) {
            return false;
        }

        // Critical temperature
        if (vitalSigns.getTemperature() != null) {
            double temp = vitalSigns.getTemperature().doubleValue();
            if (temp < 35.0 || temp > 40.0) {
                return true;
            }
        }

        // Critical blood pressure
        if (vitalSigns.getBloodPressureSystolic() != null) {
            int systolic = vitalSigns.getBloodPressureSystolic();
            if (systolic > 180 || systolic < 90) {
                return true;
            }
        }

        // Critical heart rate
        if (vitalSigns.getHeartRate() != null) {
            int hr = vitalSigns.getHeartRate();
            if (hr > 120 || hr < 50) {
                return true;
            }
        }

        // Critical oxygen saturation
        if (vitalSigns.getOxygenSaturation() != null) {
            int oxygenSat = vitalSigns.getOxygenSaturation();
            if (oxygenSat < 90) {
                return true;
            }
        }

        return false;
    }

    /**
     * توليد ملخص للسجل الطبي
     * Generate medical record summary
     */
    public static String generateRecordSummary(MedicalRecord medicalRecord) {
        if (medicalRecord == null) {
            return "";
        }

        StringBuilder summary = new StringBuilder();

        // Visit type and date
        summary.append("زيارة ").append(medicalRecord.getVisitType().getArabicName())
                .append(" في ").append(medicalRecord.getVisitDate());

        // Chief complaint
        if (medicalRecord.getChiefComplaint() != null && !medicalRecord.getChiefComplaint().trim().isEmpty()) {
            summary.append(" - الشكوى: ").append(medicalRecord.getChiefComplaint(), 0, Math.min(50, medicalRecord.getChiefComplaint().length()));
            if (medicalRecord.getChiefComplaint().length() > 50) {
                summary.append("...");
            }
        }

        // Primary diagnosis
        if (medicalRecord.getDiagnosis() != null && !medicalRecord.getDiagnosis().isEmpty()) {
            medicalRecord.getDiagnosis().stream()
                    .filter(d -> d.getIsPrimary())
                    .findFirst()
                    .ifPresent(d -> summary.append(" - التشخيص: ").append(d.getDescription()));
        }

        return summary.toString();
    }

    /**
     * التحقق من إمكانية التعديل بناء على الوقت
     * Check if record can be edited based on time constraints
     */
    public static boolean canEditBasedOnTime(MedicalRecord medicalRecord, int editingTimeoutHours) {
        if (medicalRecord == null || medicalRecord.getCreatedAt() == null) {
            return false;
        }

        // Locked or cancelled records cannot be edited
        if (medicalRecord.getStatus() == RecordStatus.LOCKED ||
                medicalRecord.getStatus() == RecordStatus.CANCELLED) {
            return false;
        }

        // Check if still within editing window
        return medicalRecord.getCreatedAt().plusHours(editingTimeoutHours).isAfter(java.time.LocalDateTime.now());
    }

    /**
     * تحديد أولوية السجل الطبي
     * Determine medical record priority
     */
    public static String determineRecordPriority(MedicalRecord medicalRecord) {
        if (medicalRecord == null) {
            return "عادي";
        }

        // Check visit type
        if (medicalRecord.getVisitType() == VisitType.EMERGENCY) {
            return "عاجل";
        }

        // Check vital signs for emergency indicators
        if (isEmergencyCase(medicalRecord.getVitalSigns())) {
            return "عاجل";
        }

        // Check if follow-up is overdue
        if (medicalRecord.getFollowUpDate() != null &&
                medicalRecord.getFollowUpDate().isBefore(LocalDate.now())) {
            return "متأخر";
        }

        return "عادي";
    }

    /**
     * إنشاء معرف مرجعي للسجل الطبي
     * Generate reference ID for medical record
     */
    public static String generateRecordReference(Long clinicId, Long patientId, LocalDate visitDate) {
        return String.format("MR-%d-%d-%s", clinicId, patientId, visitDate.toString().replace("-", ""));
    }
}