package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.ScheduleType;
import com.nakqeeb.amancare.entity.UnavailabilityType;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * طلب إنشاء جدولة طبيب
 */
@Schema(description = "طلب إنشاء جدولة طبيب")
public class CreateDoctorScheduleRequest {

    @Schema(description = "معرف الطبيب", example = "2", required = true)
    @NotNull(message = "معرف الطبيب مطلوب")
    private Long doctorId;

    @Schema(description = "أيام العمل", example = "[\"SUNDAY\", \"MONDAY\", \"TUESDAY\", \"WEDNESDAY\", \"THURSDAY\"]")
    @NotNull(message = "أيام العمل مطلوبة")
    private List<DayOfWeek> workingDays;

    @Schema(description = "وقت بداية العمل", example = "08:00:00", required = true)
    @NotNull(message = "وقت بداية العمل مطلوب")
    private LocalTime startTime;

    @Schema(description = "وقت انتهاء العمل", example = "16:00:00", required = true)
    @NotNull(message = "وقت انتهاء العمل مطلوب")
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
    private ScheduleType scheduleType = ScheduleType.REGULAR;

    @Schema(description = "ملاحظات", example = "جدول العمل الاعتيادي للطبيب")
    private String notes;

    // Constructors
    public CreateDoctorScheduleRequest() {}

    // Getters and Setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public List<DayOfWeek> getWorkingDays() { return workingDays; }
    public void setWorkingDays(List<DayOfWeek> workingDays) { this.workingDays = workingDays; }

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
}