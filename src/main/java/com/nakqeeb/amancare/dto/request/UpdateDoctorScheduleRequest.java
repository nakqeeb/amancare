package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.DurationConfigType;
import com.nakqeeb.amancare.entity.ScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * طلب تحديث جدولة طبيب
 * Request to update doctor schedule
 */
@Schema(description = "طلب تحديث جدولة طبيب")
public class UpdateDoctorScheduleRequest {

    @Schema(description = "أيام العمل (اختياري)", example = "[\"SUNDAY\", \"MONDAY\", \"TUESDAY\"]")
    private List<DayOfWeek> workingDays;

    @Schema(description = "وقت بداية العمل", example = "08:00:00")
    private LocalTime startTime;

    @Schema(description = "وقت انتهاء العمل", example = "16:00:00")
    private LocalTime endTime;

    @Schema(description = "وقت بداية استراحة الغداء", example = "12:00:00")
    private LocalTime breakStartTime;

    @Schema(description = "وقت انتهاء استراحة الغداء", example = "13:00:00")
    private LocalTime breakEndTime;

    @Schema(description = "تاريخ بداية سريان الجدول", example = "2024-09-01")
    private LocalDate effectiveDate;

    @Schema(description = "تاريخ انتهاء الجدول (للجداول المؤقتة)", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "نوع الجدول", example = "REGULAR")
    private ScheduleType scheduleType;

    @Schema(description = "ملاحظات", example = "جدول العمل المحدث")
    private String notes;

    @Schema(description = "حالة تفعيل الجدول", example = "true")
    private Boolean isActive;

    // **NEW: Duration Configuration Fields**

    @Schema(description = "نوع تكوين المدة", example = "DIRECT")
    private DurationConfigType durationConfigType;

    @Schema(description = "مدة كل موعد بالدقائق (للنوع DIRECT)", example = "30")
    @Min(value = 5, message = "المدة يجب أن تكون 5 دقائق على الأقل")
    @Max(value = 240, message = "المدة يجب ألا تتجاوز 240 دقيقة")
    private Integer durationMinutes;

    @Schema(description = "عدد المواعيد المستهدف في اليوم (للنوع TOKEN_BASED)", example = "14")
    @Min(value = 1, message = "عدد المواعيد يجب أن يكون 1 على الأقل")
    @Max(value = 100, message = "عدد المواعيد يجب ألا يتجاوز 100")
    private Integer targetTokensPerDay;

    // Constructors

    public UpdateDoctorScheduleRequest() {}

    public UpdateDoctorScheduleRequest(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters

    public List<DayOfWeek> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(List<DayOfWeek> workingDays) {
        this.workingDays = workingDays;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalTime getBreakStartTime() {
        return breakStartTime;
    }

    public void setBreakStartTime(LocalTime breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public LocalTime getBreakEndTime() {
        return breakEndTime;
    }

    public void setBreakEndTime(LocalTime breakEndTime) {
        this.breakEndTime = breakEndTime;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // **NEW: Duration Configuration Getters and Setters**

    public DurationConfigType getDurationConfigType() {
        return durationConfigType;
    }

    public void setDurationConfigType(DurationConfigType durationConfigType) {
        this.durationConfigType = durationConfigType;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getTargetTokensPerDay() {
        return targetTokensPerDay;
    }

    public void setTargetTokensPerDay(Integer targetTokensPerDay) {
        this.targetTokensPerDay = targetTokensPerDay;
    }

    // Builder pattern for convenience (optional but recommended)

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UpdateDoctorScheduleRequest request = new UpdateDoctorScheduleRequest();

        public Builder workingDays(List<DayOfWeek> workingDays) {
            request.workingDays = workingDays;
            return this;
        }

        public Builder startTime(LocalTime startTime) {
            request.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            request.endTime = endTime;
            return this;
        }

        public Builder breakStartTime(LocalTime breakStartTime) {
            request.breakStartTime = breakStartTime;
            return this;
        }

        public Builder breakEndTime(LocalTime breakEndTime) {
            request.breakEndTime = breakEndTime;
            return this;
        }

        public Builder effectiveDate(LocalDate effectiveDate) {
            request.effectiveDate = effectiveDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            request.endDate = endDate;
            return this;
        }

        public Builder scheduleType(ScheduleType scheduleType) {
            request.scheduleType = scheduleType;
            return this;
        }

        public Builder notes(String notes) {
            request.notes = notes;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            request.isActive = isActive;
            return this;
        }

        public Builder durationConfigType(DurationConfigType durationConfigType) {
            request.durationConfigType = durationConfigType;
            return this;
        }

        public Builder durationMinutes(Integer durationMinutes) {
            request.durationMinutes = durationMinutes;
            return this;
        }

        public Builder targetTokensPerDay(Integer targetTokensPerDay) {
            request.targetTokensPerDay = targetTokensPerDay;
            return this;
        }

        public UpdateDoctorScheduleRequest build() {
            return request;
        }
    }

    @Override
    public String toString() {
        return "UpdateDoctorScheduleRequest{" +
                "workingDays=" + workingDays +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", breakStartTime=" + breakStartTime +
                ", breakEndTime=" + breakEndTime +
                ", effectiveDate=" + effectiveDate +
                ", endDate=" + endDate +
                ", scheduleType=" + scheduleType +
                ", notes='" + notes + '\'' +
                ", isActive=" + isActive +
                ", durationConfigType=" + durationConfigType +
                ", durationMinutes=" + durationMinutes +
                ", targetTokensPerDay=" + targetTokensPerDay +
                '}';
    }
}