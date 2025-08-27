// =============================================================================
// Appointment Statistics DTO - إحصائيات المواعيد
// =============================================================================

package com.nakqeeb.amancare.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * إحصائيات المواعيد
 */
@Schema(description = "إحصائيات المواعيد")
public class AppointmentStatistics {

    @Schema(description = "إجمالي المواعيد", example = "25")
    private long totalAppointments;

    @Schema(description = "المواعيد المكتملة", example = "18")
    private long completedAppointments;

    @Schema(description = "المواعيد الملغية", example = "3")
    private long cancelledAppointments;

    @Schema(description = "المواعيد لم يحضر", example = "2")
    private long noShowAppointments;

    @Schema(description = "المواعيد المجدولة والمؤكدة", example = "2")
    private long pendingAppointments;

    @Schema(description = "تاريخ الإحصائيات", example = "2024-08-28")
    private LocalDate date;

    @Schema(description = "معدل الحضور", example = "78.3")
    private double attendanceRate;

    // Constructors
    public AppointmentStatistics() {}

    public AppointmentStatistics(long totalAppointments, long completedAppointments,
                                 long cancelledAppointments, long noShowAppointments, LocalDate date) {
        this.totalAppointments = totalAppointments;
        this.completedAppointments = completedAppointments;
        this.cancelledAppointments = cancelledAppointments;
        this.noShowAppointments = noShowAppointments;
        this.pendingAppointments = totalAppointments - completedAppointments - cancelledAppointments - noShowAppointments;
        this.date = date;

        // حساب معدل الحضور
        if (totalAppointments > 0) {
            this.attendanceRate = (double) completedAppointments / totalAppointments * 100;
        } else {
            this.attendanceRate = 0.0;
        }
    }

    // Getters and Setters
    public long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(long totalAppointments) { this.totalAppointments = totalAppointments; }

    public long getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(long completedAppointments) { this.completedAppointments = completedAppointments; }

    public long getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(long cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }

    public long getNoShowAppointments() { return noShowAppointments; }
    public void setNoShowAppointments(long noShowAppointments) { this.noShowAppointments = noShowAppointments; }

    public long getPendingAppointments() { return pendingAppointments; }
    public void setPendingAppointments(long pendingAppointments) { this.pendingAppointments = pendingAppointments; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
}