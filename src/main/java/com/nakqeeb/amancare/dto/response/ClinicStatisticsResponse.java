package com.nakqeeb.amancare.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClinicStatisticsResponse {
    // Clinic Info
    private Long clinicId;
    private String clinicName;

    // User Statistics
    private Long totalUsers;
    private Long activeUsers;
    private Long totalDoctors;
    private Long totalNurses;
    private Long totalReceptionists;

    // Patient Statistics
    private Long totalPatients;
    private Long activePatients;
    private Long newPatientsThisMonth;
    private Long deletedPatients;

    // Appointment Statistics
    private Long totalAppointments;
    private Long todayAppointments;
    private Long upcomingAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;
    private Double appointmentCompletionRate;

    // Financial Statistics
    private java.math.BigDecimal totalRevenue;
    private java.math.BigDecimal monthlyRevenue;
    private java.math.BigDecimal outstandingBalance;
    private Long totalInvoices;
    private Long paidInvoices;
    private Long overdueInvoices;

    // Subscription Info
    private String subscriptionPlan;
    private LocalDate subscriptionEndDate;
    private Integer daysUntilExpiry;
    private Boolean isActive;

    // Usage Metrics
    private Double storageUsedGB;
    private Double storageQuotaGB;
    private Integer apiCallsThisMonth;
    private Integer apiCallsQuota;
}