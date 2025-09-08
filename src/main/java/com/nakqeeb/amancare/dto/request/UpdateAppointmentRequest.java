package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.AppointmentType;
import com.nakqeeb.amancare.validation.FutureOrTodayWithTime;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
/**
 * طلب تحديث موعد
 */
@Schema(description = "طلب تحديث موعد")
@FutureOrTodayWithTime(dateField = "appointmentDate", timeField = "appointmentTime")
public class UpdateAppointmentRequest {

    @Schema(description = "تاريخ الموعد الجديد", example = "2024-08-29")
    private LocalDate appointmentDate;

    @Schema(description = "وقت الموعد الجديد", example = "14:00:00")
    private LocalTime appointmentTime;

    @Schema(description = "مدة الموعد بالدقائق", example = "45")
    @Positive(message = "مدة الموعد يجب أن تكون رقماً موجباً")
    private Integer durationMinutes;

    @Schema(description = "نوع الموعد", example = "FOLLOW_UP")
    private AppointmentType appointmentType;

    @Schema(description = "الشكوى الرئيسية", example = "متابعة العلاج والتحسن")
    @Size(max = 500, message = "الشكوى الرئيسية يجب أن تكون أقل من 500 حرف")
    private String chiefComplaint;

    @Schema(description = "ملاحظات إضافية", example = "المريض يشعر بتحسن ملحوظ")
    @Size(max = 1000, message = "الملاحظات يجب أن تكون أقل من 1000 حرف")
    private String notes;

    // Constructors
    public UpdateAppointmentRequest() {}

    // Getters and Setters
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public AppointmentType getAppointmentType() { return appointmentType; }
    public void setAppointmentType(AppointmentType appointmentType) { this.appointmentType = appointmentType; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}