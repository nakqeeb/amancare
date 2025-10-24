package com.nakqeeb.amancare.dto.request;

import com.nakqeeb.amancare.entity.AppointmentType;
import com.nakqeeb.amancare.validation.FutureOrTodayWithTime;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * طلب إنشاء موعد جديد
 */
@Schema(description = "طلب إنشاء موعد جديد")
@FutureOrTodayWithTime(dateField = "appointmentDate", timeField = "appointmentTime")
public class CreateAppointmentRequest {

    @Schema(description = "معرف المريض", example = "1", required = true)
    @NotNull(message = "معرف المريض مطلوب")
    @Positive(message = "معرف المريض يجب أن يكون رقماً موجباً")
    private Long patientId;

    @Schema(description = "معرف الطبيب", example = "2", required = true)
    @NotNull(message = "معرف الطبيب مطلوب")
    @Positive(message = "معرف الطبيب يجب أن يكون رقماً موجباً")
    private Long doctorId;

    @Schema(description = "تاريخ الموعد", example = "2024-08-28", required = true)
    @NotNull(message = "تاريخ الموعد مطلوب")
    // Removed @Future annotation - validation is now handled by @FutureOrTodayWithTime at class level
    // @Future(message = "تاريخ الموعد يجب أن يكون في المستقبل")
    private LocalDate appointmentDate;

    @Schema(description = "وقت الموعد", example = "10:30:00", required = true)
    @NotNull(message = "وقت الموعد مطلوب")
    private LocalTime appointmentTime;

    @Schema(description = "مدة الموعد بالدقائق (اختياري - يتم أخذها من جدول الطبيب تلقائياً)", example = "30")
    private Integer durationMinutes; // No longer required

    @Schema(description = "تجاوز مدة الموعد (اختياري)", example = "60")
    private Integer overrideDurationMinutes;

    @Schema(description = "سبب تجاوز المدة", example = "استشارة ممتدة")
    private String overrideReason;

    @Schema(description = "نوع الموعد", example = "CONSULTATION")
    private AppointmentType appointmentType = AppointmentType.CONSULTATION;

    @Schema(description = "الشكوى الرئيسية", example = "ألم في الصدر وصعوبة في التنفس")
    @Size(max = 500, message = "الشكوى الرئيسية يجب أن تكون أقل من 500 حرف")
    private String chiefComplaint;

    @Schema(description = "ملاحظات إضافية", example = "المريض يفضل الفحص السريع")
    @Size(max = 1000, message = "الملاحظات يجب أن تكون أقل من 1000 حرف")
    private String notes;

    // Constructors
    public CreateAppointmentRequest() {}

    // Getters and Setters
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getOverrideDurationMinutes() { return overrideDurationMinutes; }
    public void setOverrideDurationMinutes(Integer overrideDurationMinutes) {
        this.overrideDurationMinutes = overrideDurationMinutes;
    }

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) {
        this.overrideReason = overrideReason;
    }
    public AppointmentType getAppointmentType() { return appointmentType; }
    public void setAppointmentType(AppointmentType appointmentType) { this.appointmentType = appointmentType; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}