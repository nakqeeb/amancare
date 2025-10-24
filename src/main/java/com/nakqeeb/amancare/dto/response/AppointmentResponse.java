package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.Appointment;
import com.nakqeeb.amancare.entity.AppointmentStatus;
import com.nakqeeb.amancare.entity.AppointmentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * استجابة بيانات الموعد الكاملة
 */
@Schema(description = "بيانات الموعد الكاملة")
public class AppointmentResponse {

    @Schema(description = "معرف الموعد", example = "1")
    private Long id;

    @Schema(description = "بيانات المريض")
    private AppointmentPatientInfo patient;

    @Schema(description = "بيانات الطبيب")
    private AppointmentDoctorInfo doctor;

    @Schema(description = "تاريخ الموعد", example = "2024-08-28")
    private LocalDate appointmentDate;

    @Schema(description = "وقت الموعد", example = "10:30:00")
    private LocalTime appointmentTime;

    @Schema(description = "مدة الموعد بالدقائق", example = "30")
    private Integer durationMinutes;

    @Schema(description = "هل تم تجاوز المدة", example = "false")
    private Boolean isDurationOverridden;

    @Schema(description = "المدة الأصلية من الجدول", example = "30")
    private Integer originalDurationMinutes;

    @Schema(description = "سبب تجاوز المدة", example = "استشارة ممتدة")
    private String overrideReason;

    @Schema(description = "رقم الرمز (Token)", example = "5")
    private Integer tokenNumber;

    @Schema(description = "نوع الموعد", example = "CONSULTATION")
    private AppointmentType appointmentType;

    @Schema(description = "حالة الموعد", example = "SCHEDULED")
    private AppointmentStatus status;

    @Schema(description = "الشكوى الرئيسية", example = "ألم في الصدر")
    private String chiefComplaint;

    @Schema(description = "ملاحظات", example = "المريض يفضل الفحص السريع")
    private String notes;

    @Schema(description = "تاريخ الإنشاء", example = "2024-08-27T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "تاريخ آخر تحديث", example = "2024-08-27T15:20:00")
    private LocalDateTime updatedAt;

    @Schema(description = "منشئ الموعد")
    private String createdBy;

    // Constructors
    public AppointmentResponse() {}

    /**
     * إنشاء AppointmentResponse من Appointment entity
     */
    public static AppointmentResponse fromAppointment(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());

        // معلومات المريض
        AppointmentPatientInfo patientInfo = new AppointmentPatientInfo();
        patientInfo.setId(appointment.getPatient().getId());
        patientInfo.setPatientNumber(appointment.getPatient().getPatientNumber());
        patientInfo.setFullName(appointment.getPatient().getFullName());
        patientInfo.setPhone(appointment.getPatient().getPhone());
        patientInfo.setAge(appointment.getPatient().getAge());
        response.setPatient(patientInfo);

        // معلومات الطبيب
        AppointmentDoctorInfo doctorInfo = new AppointmentDoctorInfo();
        doctorInfo.setId(appointment.getDoctor().getId());
        doctorInfo.setFullName(appointment.getDoctor().getFullName());
        doctorInfo.setSpecialization(appointment.getDoctor().getSpecialization());
        response.setDoctor(doctorInfo);

        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setDurationMinutes(appointment.getDurationMinutes());
        response.setIsDurationOverridden(appointment.getIsDurationOverridden());
        response.setOriginalDurationMinutes(appointment.getOriginalDurationMinutes());
        response.setOverrideReason(appointment.getOverrideReason());
        response.setTokenNumber(appointment.getTokenNumber());
        response.setAppointmentType(appointment.getAppointmentType());
        response.setStatus(appointment.getStatus());
        response.setChiefComplaint(appointment.getChiefComplaint());
        response.setNotes(appointment.getNotes());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        response.setCreatedBy(appointment.getCreatedBy().getFullName());

        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppointmentPatientInfo getPatient() { return patient; }
    public void setPatient(AppointmentPatientInfo patient) { this.patient = patient; }

    public AppointmentDoctorInfo getDoctor() { return doctor; }
    public void setDoctor(AppointmentDoctorInfo doctor) { this.doctor = doctor; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Boolean getIsDurationOverridden() { return isDurationOverridden; }
    public void setIsDurationOverridden(Boolean isDurationOverridden) {
        this.isDurationOverridden = isDurationOverridden;
    }

    public Integer getOriginalDurationMinutes() { return originalDurationMinutes; }
    public void setOriginalDurationMinutes(Integer originalDurationMinutes) {
        this.originalDurationMinutes = originalDurationMinutes;
    }

    public String getOverrideReason() { return overrideReason; }
    public void setOverrideReason(String overrideReason) {
        this.overrideReason = overrideReason;
    }

    public Integer getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(Integer tokenNumber) { this.tokenNumber = tokenNumber; }

    public AppointmentType getAppointmentType() { return appointmentType; }
    public void setAppointmentType(AppointmentType appointmentType) { this.appointmentType = appointmentType; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}