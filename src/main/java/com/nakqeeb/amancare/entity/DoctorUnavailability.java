// =============================================================================
// Doctor Unavailability Entity - كيان عدم توفر الطبيب
// =============================================================================

package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * كيان عدم توفر الطبيب (إجازات، مؤتمرات، طوارئ)
 */
@Entity
@Table(name = "doctor_unavailability",
        indexes = {
                @Index(name = "idx_doctor_unavailable_date", columnList = "doctor_id, unavailable_date")
        })
public class DoctorUnavailability extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @NotNull(message = "تاريخ عدم التوفر مطلوب")
    @Column(name = "unavailable_date", nullable = false)
    private LocalDate unavailableDate;

    @Column(name = "start_time")
    private LocalTime startTime; // null = طوال اليوم

    @Column(name = "end_time")
    private LocalTime endTime;   // null = طوال اليوم

    @Enumerated(EnumType.STRING)
    @Column(name = "unavailability_type")
    private UnavailabilityType unavailabilityType;

    @Column(name = "reason")
    private String reason;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Column(name = "recurrence_end_date")
    private LocalDate recurrenceEndDate;

    // Constructors
    public DoctorUnavailability() {}

    public DoctorUnavailability(User doctor, LocalDate unavailableDate,
                                UnavailabilityType type, String reason) {
        this.doctor = doctor;
        this.unavailableDate = unavailableDate;
        this.unavailabilityType = type;
        this.reason = reason;
    }

    // Helper Methods
    public boolean isUnavailableAt(LocalTime time) {
        if (startTime == null && endTime == null) {
            return true; // غير متاح طوال اليوم
        }

        if (startTime != null && endTime != null) {
            return !time.isBefore(startTime) && !time.isAfter(endTime);
        }

        return false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDoctor() { return doctor; }
    public void setDoctor(User doctor) { this.doctor = doctor; }

    public LocalDate getUnavailableDate() { return unavailableDate; }
    public void setUnavailableDate(LocalDate unavailableDate) { this.unavailableDate = unavailableDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public UnavailabilityType getUnavailabilityType() { return unavailabilityType; }
    public void setUnavailabilityType(UnavailabilityType unavailabilityType) { this.unavailabilityType = unavailabilityType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Boolean getIsRecurring() { return isRecurring; }
    public void setIsRecurring(Boolean isRecurring) { this.isRecurring = isRecurring; }

    public LocalDate getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }
}
