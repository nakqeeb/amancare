// =============================================================================
// Doctor Schedule Entity - كيان جدولة الأطباء
// =============================================================================

package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * كيان جدولة الطبيب
 */
@Entity
@Table(name = "doctor_schedules",
        indexes = {
                @Index(name = "idx_doctor_day", columnList = "doctor_id, day_of_week"),
                @Index(name = "idx_doctor_date", columnList = "doctor_id, effective_date")
        })
public class DoctorSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @NotNull(message = "وقت البداية مطلوب")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "وقت النهاية مطلوب")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    @Column(name = "effective_date")
    private LocalDate effectiveDate; // تاريخ بداية سريان هذا الجدول

    @Column(name = "end_date")
    private LocalDate endDate; // تاريخ انتهاء (للجداول المؤقتة)

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type")
    private ScheduleType scheduleType = ScheduleType.REGULAR;

    @Column(name = "notes")
    private String notes;

    // Constructors
    public DoctorSchedule() {}

    public DoctorSchedule(User doctor, DayOfWeek dayOfWeek, LocalTime startTime,
                          LocalTime endTime, ScheduleType scheduleType) {
        this.doctor = doctor;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scheduleType = scheduleType;
    }

    // Helper Methods
    public boolean isAvailableAt(LocalTime time) {
        if (!isActive) return false;

        // التحقق من الوقت الأساسي
        boolean inWorkingHours = !time.isBefore(startTime) && !time.isAfter(endTime);

        // التحقق من استراحة الغداء إن وجدت
        if (breakStartTime != null && breakEndTime != null) {
            boolean inBreakTime = !time.isBefore(breakStartTime) && !time.isAfter(breakEndTime);
            return inWorkingHours && !inBreakTime;
        }

        return inWorkingHours;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDoctor() { return doctor; }
    public void setDoctor(User doctor) { this.doctor = doctor; }

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

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public ScheduleType getScheduleType() { return scheduleType; }
    public void setScheduleType(ScheduleType scheduleType) { this.scheduleType = scheduleType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}