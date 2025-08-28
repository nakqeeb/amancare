package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.UnavailabilityType;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * طلب إضافة عدم توفر
 */
@Schema(description = "طلب إضافة عدم توفر للطبيب")
public class CreateUnavailabilityRequest {

    @Schema(description = "معرف الطبيب", example = "2", required = true)
    @NotNull(message = "معرف الطبيب مطلوب")
    private Long doctorId;

    @Schema(description = "تاريخ عدم التوفر", example = "2024-09-15", required = true)
    @NotNull(message = "تاريخ عدم التوفر مطلوب")
    private LocalDate unavailableDate;

    @Schema(description = "وقت بداية عدم التوفر (فارغ = طوال اليوم)", example = "10:00:00")
    private LocalTime startTime;

    @Schema(description = "وقت انتهاء عدم التوفر (فارغ = طوال اليوم)", example = "14:00:00")
    private LocalTime endTime;

    @Schema(description = "نوع عدم التوفر", example = "VACATION", required = true)
    @NotNull(message = "نوع عدم التوفر مطلوب")
    private UnavailabilityType unavailabilityType;

    @Schema(description = "سبب عدم التوفر", example = "إجازة سنوية")
    private String reason;

    @Schema(description = "هل هو متكرر", example = "false")
    private Boolean isRecurring = false;

    @Schema(description = "تاريخ انتهاء التكرار", example = "2024-09-30")
    private LocalDate recurrenceEndDate;

    // Constructors
    public CreateUnavailabilityRequest() {}

    // Getters and Setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

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