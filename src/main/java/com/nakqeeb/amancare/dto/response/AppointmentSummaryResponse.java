package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.Appointment;
import com.nakqeeb.amancare.entity.AppointmentStatus;
import com.nakqeeb.amancare.entity.AppointmentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ملخص الموعد (للقوائم والتقويم)
 */
@Schema(description = "ملخص بيانات الموعد")
public class AppointmentSummaryResponse {

    @Schema(description = "معرف الموعد", example = "1")
    private Long id;

    @Schema(description = "اسم المريض الكامل", example = "محمد أحمد")
    private String patientName;

    @Schema(description = "رقم المريض", example = "P202401001")
    private String patientNumber;

    @Schema(description = "اسم الطبيب الكامل", example = "د. أحمد محمد")
    private String doctorName;

    @Schema(description = "تاريخ الموعد", example = "2024-08-28")
    private LocalDate appointmentDate;

    @Schema(description = "وقت الموعد", example = "10:30:00")
    private LocalTime appointmentTime;

    @Schema(description = "مدة الموعد", example = "30")
    private Integer durationMinutes;

    @Schema(description = "نوع الموعد", example = "CONSULTATION")
    private AppointmentType appointmentType;

    @Schema(description = "حالة الموعد", example = "SCHEDULED")
    private AppointmentStatus status;

    @Schema(description = "الشكوى الرئيسية (مختصرة)", example = "ألم في الصدر")
    private String chiefComplaint;

    // Constructors
    public AppointmentSummaryResponse() {}

    /**
     * إنشاء AppointmentSummaryResponse من Appointment entity
     */
    public static AppointmentSummaryResponse fromAppointment(Appointment appointment) {
        AppointmentSummaryResponse summary = new AppointmentSummaryResponse();
        summary.setId(appointment.getId());
        summary.setPatientName(appointment.getPatient().getFullName());
        summary.setPatientNumber(appointment.getPatient().getPatientNumber());
        summary.setDoctorName(appointment.getDoctor().getFullName());
        summary.setAppointmentDate(appointment.getAppointmentDate());
        summary.setAppointmentTime(appointment.getAppointmentTime());
        summary.setDurationMinutes(appointment.getDurationMinutes());
        summary.setAppointmentType(appointment.getAppointmentType());
        summary.setStatus(appointment.getStatus());
        summary.setChiefComplaint(appointment.getChiefComplaint());
        return summary;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientNumber() { return patientNumber; }
    public void setPatientNumber(String patientNumber) { this.patientNumber = patientNumber; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public AppointmentType getAppointmentType() { return appointmentType; }
    public void setAppointmentType(AppointmentType appointmentType) { this.appointmentType = appointmentType; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
}
