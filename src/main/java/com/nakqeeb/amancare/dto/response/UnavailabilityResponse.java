package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.DoctorUnavailability;
import com.nakqeeb.amancare.entity.UnavailabilityType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * استجابة عدم التوفر
 */
@Schema(description = "عدم توفر الطبيب")
public class UnavailabilityResponse {

    @Schema(description = "معرف عدم التوفر", example = "1")
    private Long id;

    @Schema(description = "معرف الطبيب", example = "2")
    private Long doctorId;

    @Schema(description = "اسم الطبيب", example = "د. أحمد محمد")
    private String doctorName;

    @Schema(description = "تاريخ عدم التوفر", example = "2024-09-15")
    private LocalDate unavailableDate;

    @Schema(description = "وقت البداية", example = "10:00:00")
    private LocalTime startTime;

    @Schema(description = "وقت النهاية", example = "14:00:00")
    private LocalTime endTime;

    @Schema(description = "نوع عدم التوفر", example = "VACATION")
    private UnavailabilityType unavailabilityType;

    @Schema(description = "السبب", example = "إجازة سنوية")
    private String reason;

    @Schema(description = "هل هو متكرر", example = "false")
    private boolean recurring;

    @Schema(description = "تاريخ انتهاء التكرار", example = "2024-09-30")
    private LocalDate recurrenceEndDate;

    // Constructors
    public UnavailabilityResponse() {}

    /**
     * إنشاء من DoctorUnavailability entity
     */
    public static UnavailabilityResponse fromEntity(DoctorUnavailability unavailability) {
        UnavailabilityResponse response = new UnavailabilityResponse();
        response.setId(unavailability.getId());
        response.setDoctorId(unavailability.getDoctor().getId());
        response.setDoctorName(unavailability.getDoctor().getFullName());
        response.setUnavailableDate(unavailability.getUnavailableDate());
        response.setStartTime(unavailability.getStartTime());
        response.setEndTime(unavailability.getEndTime());
        response.setUnavailabilityType(unavailability.getUnavailabilityType());
        response.setReason(unavailability.getReason());
        response.setRecurring(unavailability.getIsRecurring());
        response.setRecurrenceEndDate(unavailability.getRecurrenceEndDate());
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

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

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }

    public LocalDate getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(LocalDate recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }
}