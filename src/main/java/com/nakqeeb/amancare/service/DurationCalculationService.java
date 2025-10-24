package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.entity.DoctorSchedule;
import com.nakqeeb.amancare.entity.DurationConfigType;
import com.nakqeeb.amancare.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * خدمة حساب مدة المواعيد
 * Service for calculating appointment durations
 */
@Service
public class DurationCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(DurationCalculationService.class);

    private static final int MIN_DURATION_MINUTES = 5;
    private static final int MAX_DURATION_MINUTES = 240;
    private static final int DURATION_ROUNDING_INTERVAL = 5;

    /**
     * Calculate duration from target tokens per day
     *
     * @param schedule The doctor schedule
     * @return Calculated duration in minutes, rounded to nearest 5 minutes
     */
    public int calculateDurationFromTokens(DoctorSchedule schedule) {
        if (schedule.getTargetTokensPerDay() == null || schedule.getTargetTokensPerDay() <= 0) {
            throw new BadRequestException("عدد المواعيد المستهدف يجب أن يكون أكبر من صفر");
        }

        int availableMinutes = schedule.calculateAvailableWorkingMinutes();

        if (availableMinutes <= 0) {
            throw new BadRequestException("وقت العمل المتاح غير صحيح");
        }

        // Calculate raw duration
        double rawDuration = (double) availableMinutes / schedule.getTargetTokensPerDay();

        // Round to nearest 5 minutes for practical scheduling
        int roundedDuration = roundToNearestInterval(rawDuration, DURATION_ROUNDING_INTERVAL);

        // Validate the result
        if (roundedDuration < MIN_DURATION_MINUTES) {
            throw new BadRequestException(
                    String.format("عدد المواعيد المستهدف (%d) كبير جداً. المدة المحسوبة (%d دقيقة) أقل من الحد الأدنى (%d دقائق)",
                            schedule.getTargetTokensPerDay(), roundedDuration, MIN_DURATION_MINUTES)
            );
        }

        if (roundedDuration > MAX_DURATION_MINUTES) {
            throw new BadRequestException(
                    String.format("عدد المواعيد المستهدف (%d) صغير جداً. المدة المحسوبة (%d دقيقة) تتجاوز الحد الأقصى (%d دقائق)",
                            schedule.getTargetTokensPerDay(), roundedDuration, MAX_DURATION_MINUTES)
            );
        }

        logger.info("Calculated duration: {} minutes for {} tokens in {} available minutes",
                roundedDuration, schedule.getTargetTokensPerDay(), availableMinutes);

        return roundedDuration;
    }

    /**
     * Calculate expected number of tokens based on direct duration
     *
     * @param schedule The doctor schedule
     * @return Expected number of tokens
     */
    public int calculateExpectedTokens(DoctorSchedule schedule) {
        if (schedule.getDurationMinutes() == null || schedule.getDurationMinutes() <= 0) {
            throw new BadRequestException("مدة الموعد يجب أن تكون أكبر من صفر");
        }

        int availableMinutes = schedule.calculateAvailableWorkingMinutes();
        return availableMinutes / schedule.getDurationMinutes();
    }

    /**
     * Validate duration configuration
     *
     * @param schedule The doctor schedule to validate
     * @throws BadRequestException if configuration is invalid
     */
    public void validateDurationConfiguration(DoctorSchedule schedule) {
        if (schedule.getDurationConfigType() == null) {
            throw new BadRequestException("نوع تكوين المدة مطلوب");
        }

        switch (schedule.getDurationConfigType()) {
            case DIRECT:
                validateDirectDuration(schedule);
                break;
            case TOKEN_BASED:
                validateTokenBasedDuration(schedule);
                break;
            default:
                throw new BadRequestException("نوع تكوين المدة غير صحيح");
        }
    }

    /**
     * Apply duration configuration to schedule
     * Calculates and sets appropriate duration based on config type
     *
     * @param schedule The doctor schedule
     */
    public void applyDurationConfiguration(DoctorSchedule schedule) {
        validateDurationConfiguration(schedule);

        if (schedule.getDurationConfigType() == DurationConfigType.TOKEN_BASED) {
            // Calculate duration from tokens
            int calculatedDuration = calculateDurationFromTokens(schedule);
            schedule.setCalculatedDurationMinutes(calculatedDuration);

            logger.info("Applied TOKEN_BASED configuration: {} tokens = {} minutes per appointment",
                    schedule.getTargetTokensPerDay(), calculatedDuration);
        } else {
            // Direct duration - ensure calculated is null
            schedule.setCalculatedDurationMinutes(null);
            schedule.setTargetTokensPerDay(null);

            logger.info("Applied DIRECT configuration: {} minutes per appointment",
                    schedule.getDurationMinutes());
        }
    }

    // =============================================================================
    // Private Helper Methods
    // =============================================================================

    private void validateDirectDuration(DoctorSchedule schedule) {
        if (schedule.getDurationMinutes() == null || schedule.getDurationMinutes() <= 0) {
            throw new BadRequestException("مدة الموعد مطلوبة لنوع التكوين المباشر");
        }

        if (schedule.getDurationMinutes() < MIN_DURATION_MINUTES) {
            throw new BadRequestException(
                    String.format("مدة الموعد يجب أن تكون %d دقائق على الأقل", MIN_DURATION_MINUTES)
            );
        }

        if (schedule.getDurationMinutes() > MAX_DURATION_MINUTES) {
            throw new BadRequestException(
                    String.format("مدة الموعد يجب ألا تتجاوز %d دقيقة", MAX_DURATION_MINUTES)
            );
        }

        int availableMinutes = schedule.calculateAvailableWorkingMinutes();
        if (schedule.getDurationMinutes() > availableMinutes) {
            throw new BadRequestException("مدة الموعد تتجاوز وقت العمل المتاح");
        }
    }

    private void validateTokenBasedDuration(DoctorSchedule schedule) {
        if (schedule.getTargetTokensPerDay() == null || schedule.getTargetTokensPerDay() <= 0) {
            throw new BadRequestException("عدد المواعيد المستهدف مطلوب لنوع التكوين المبني على الرموز");
        }

        int availableMinutes = schedule.calculateAvailableWorkingMinutes();
        int minimumTokens = availableMinutes / MAX_DURATION_MINUTES;
        int maximumTokens = availableMinutes / MIN_DURATION_MINUTES;

        if (schedule.getTargetTokensPerDay() < minimumTokens) {
            throw new BadRequestException(
                    String.format("عدد المواعيد المستهدف قليل جداً. الحد الأدنى: %d موعد", minimumTokens)
            );
        }

        if (schedule.getTargetTokensPerDay() > maximumTokens) {
            throw new BadRequestException(
                    String.format("عدد المواعيد المستهدف كبير جداً. الحد الأقصى: %d موعد", maximumTokens)
            );
        }
    }

    private int roundToNearestInterval(double value, int interval) {
        return (int) (Math.round(value / interval) * interval);
    }
}