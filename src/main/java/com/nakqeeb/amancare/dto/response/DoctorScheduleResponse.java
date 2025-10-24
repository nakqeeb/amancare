// =============================================================================
// Response DTOs - كائنات الاستجابة
// =============================================================================

package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.DoctorSchedule;
import com.nakqeeb.amancare.entity.ScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * استجابة جدولة الطبيب
 */
@Schema(description = "جدولة الطبيب")
public class DoctorScheduleResponse {

    @Schema(description = "معرف الجدول", example = "1")
    private Long id;

    @Schema(description = "معرف الطبيب", example = "2")
    private Long doctorId;

    @Schema(description = "اسم الطبيب", example = "د. أحمد محمد")
    private String doctorName;

    @Schema(description = "يوم الأسبوع", example = "SUNDAY")
    private DayOfWeek dayOfWeek;

    @Schema(description = "وقت البداية", example = "08:00:00")
    private LocalTime startTime;

    @Schema(description = "وقت النهاية", example = "16:00:00")
    private LocalTime endTime;

    @Schema(description = "وقت بداية الاستراحة", example = "12:00:00")
    private LocalTime breakStartTime;

    @Schema(description = "وقت نهاية الاستراحة", example = "13:00:00")
    private LocalTime breakEndTime;

    @Schema(description = "تاريخ بداية السريان", example = "2024-09-01")
    private LocalDate effectiveDate;

    @Schema(description = "تاريخ الانتهاء", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "نوع الجدول", example = "REGULAR")
    private ScheduleType scheduleType;

    @Schema(description = "ملاحظات", example = "جدول العمل الاعتيادي")
    private String notes;

    @Schema(description = "حالة النشاط", example = "true")
    private boolean isActive;

    @Schema(description = "مدة كل موعد بالدقائق", example = "30")
    private Integer durationMinutes;

    @Schema(description = "نوع تكوين المدة", example = "DIRECT")
    private String durationConfigType;

    @Schema(description = "عدد المواعيد المستهدف في اليوم", example = "14")
    private Integer targetTokensPerDay;

    @Schema(description = "المدة المحسوبة (للنوع TOKEN_BASED)", example = "30")
    private Integer calculatedDurationMinutes;

    @Schema(description = "المدة الفعلية المستخدمة", example = "30")
    private Integer effectiveDuration;

    @Schema(description = "عدد الدقائق المتاحة للعمل", example = "420")
    private Integer availableWorkingMinutes;

    @Schema(description = "عدد المواعيد المتوقع", example = "14")
    private Integer expectedTokens;

    // Constructors
    public DoctorScheduleResponse() {}

    /**
     * إنشاء من DoctorSchedule entity
     */
    public static DoctorScheduleResponse fromEntity(DoctorSchedule schedule) {
        DoctorScheduleResponse response = new DoctorScheduleResponse();
        response.setId(schedule.getId());
        response.setDoctorId(schedule.getDoctor().getId());
        response.setDoctorName(schedule.getDoctor().getFullName());
        response.setDayOfWeek(schedule.getDayOfWeek());
        response.setStartTime(schedule.getStartTime());
        response.setEndTime(schedule.getEndTime());
        response.setBreakStartTime(schedule.getBreakStartTime());
        response.setBreakEndTime(schedule.getBreakEndTime());
        response.setEffectiveDate(schedule.getEffectiveDate());
        response.setEndDate(schedule.getEndDate());
        response.setScheduleType(schedule.getScheduleType());
        response.setNotes(schedule.getNotes());
        response.setIsActive(schedule.getIsActive());
        response.setDurationMinutes(schedule.getDurationMinutes());
        response.setDurationConfigType(schedule.getDurationConfigType() != null ?
                schedule.getDurationConfigType().name() : null);
        response.setTargetTokensPerDay(schedule.getTargetTokensPerDay());
        response.setCalculatedDurationMinutes(schedule.getCalculatedDurationMinutes());
        response.setEffectiveDuration(schedule.getEffectiveDuration());
        response.setAvailableWorkingMinutes(schedule.calculateAvailableWorkingMinutes());

        // Calculate expected tokens
        if (schedule.getEffectiveDuration() != null && schedule.getEffectiveDuration() > 0) {
            response.setExpectedTokens(
                    schedule.calculateAvailableWorkingMinutes() / schedule.getEffectiveDuration()
            );
        }

        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public LocalTime getBreakStartTime() { return breakStartTime; }
    public void setBreakStartTime(LocalTime breakStartTime) { this.breakStartTime = breakStartTime; }

    public LocalTime getBreakEndTime() { return breakEndTime; }
    public void setBreakEndTime(LocalTime breakEndTime) { this.breakEndTime = breakEndTime; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public ScheduleType getScheduleType() { return scheduleType; }
    public void setScheduleType(ScheduleType scheduleType) { this.scheduleType = scheduleType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getDurationConfigType() { return durationConfigType; }
    public void setDurationConfigType(String durationConfigType) {
        this.durationConfigType = durationConfigType;
    }

    public Integer getTargetTokensPerDay() { return targetTokensPerDay; }
    public void setTargetTokensPerDay(Integer targetTokensPerDay) {
        this.targetTokensPerDay = targetTokensPerDay;
    }

    public Integer getCalculatedDurationMinutes() { return calculatedDurationMinutes; }
    public void setCalculatedDurationMinutes(Integer calculatedDurationMinutes) {
        this.calculatedDurationMinutes = calculatedDurationMinutes;
    }

    public Integer getEffectiveDuration() { return effectiveDuration; }
    public void setEffectiveDuration(Integer effectiveDuration) {
        this.effectiveDuration = effectiveDuration;
    }

    public Integer getAvailableWorkingMinutes() { return availableWorkingMinutes; }
    public void setAvailableWorkingMinutes(Integer availableWorkingMinutes) {
        this.availableWorkingMinutes = availableWorkingMinutes;
    }

    public Integer getExpectedTokens() { return expectedTokens; }
    public void setExpectedTokens(Integer expectedTokens) {
        this.expectedTokens = expectedTokens;
    }
}