// =============================================================================
// PatientStatistics DTO - Enhanced with Gender and Blood Type Breakdown
// =============================================================================

package com.nakqeeb.amancare.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enhanced Patient Statistics Response DTO
 * إحصائيات المرضى المحسنة
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Enhanced patient statistics with detailed breakdown")
public class PatientStatistics {

    @Schema(description = "Total number of patients", example = "150")
    private long totalPatients;

    @Schema(description = "Number of active patients", example = "140")
    private long activePatients;

    @Schema(description = "Number of inactive patients", example = "10")
    private long inactivePatients;

    @Schema(description = "New patients registered this month", example = "15")
    private long newPatientsThisMonth;

    @Schema(description = "Number of male patients", example = "75")
    private long malePatients;

    @Schema(description = "Number of female patients", example = "75")
    private long femalePatients;

    @Schema(description = "Average age of patients", example = "35.5")
    private double averageAge;

    @Schema(description = "Number of patients with appointments today", example = "12")
    private long patientsWithAppointmentsToday;

    @Schema(description = "Number of patients with pending invoices", example = "8")
    private long patientsWithPendingInvoices;

    @Schema(description = "Total outstanding balance", example = "5000.00")
    private double totalOutstandingBalance;

    // Blood type distribution (optional detailed statistics)
    @Schema(description = "Number of O+ patients", example = "30")
    private Long oPositiveCount;

    @Schema(description = "Number of O- patients", example = "5")
    private Long oNegativeCount;

    @Schema(description = "Number of A+ patients", example = "25")
    private Long aPositiveCount;

    @Schema(description = "Number of A- patients", example = "4")
    private Long aNegativeCount;

    @Schema(description = "Number of B+ patients", example = "20")
    private Long bPositiveCount;

    @Schema(description = "Number of B- patients", example = "3")
    private Long bNegativeCount;

    @Schema(description = "Number of AB+ patients", example = "10")
    private Long abPositiveCount;

    @Schema(description = "Number of AB- patients", example = "2")
    private Long abNegativeCount;

    // Constructors
    public PatientStatistics() {
    }

    public PatientStatistics(long totalPatients, long newPatientsThisMonth) {
        this.totalPatients = totalPatients;
        this.newPatientsThisMonth = newPatientsThisMonth;
        this.activePatients = totalPatients; // Default assumption
    }

    public PatientStatistics(long totalPatients, long activePatients, long inactivePatients,
                             long newPatientsThisMonth, long malePatients, long femalePatients) {
        this.totalPatients = totalPatients;
        this.activePatients = activePatients;
        this.inactivePatients = inactivePatients;
        this.newPatientsThisMonth = newPatientsThisMonth;
        this.malePatients = malePatients;
        this.femalePatients = femalePatients;
    }

    // Builder pattern for easy construction
    public static class Builder {
        private PatientStatistics stats = new PatientStatistics();

        public Builder totalPatients(long totalPatients) {
            stats.totalPatients = totalPatients;
            return this;
        }

        public Builder activePatients(long activePatients) {
            stats.activePatients = activePatients;
            return this;
        }

        public Builder inactivePatients(long inactivePatients) {
            stats.inactivePatients = inactivePatients;
            return this;
        }

        public Builder newPatientsThisMonth(long newPatientsThisMonth) {
            stats.newPatientsThisMonth = newPatientsThisMonth;
            return this;
        }

        public Builder malePatients(long malePatients) {
            stats.malePatients = malePatients;
            return this;
        }

        public Builder femalePatients(long femalePatients) {
            stats.femalePatients = femalePatients;
            return this;
        }

        public Builder averageAge(double averageAge) {
            stats.averageAge = averageAge;
            return this;
        }

        public Builder patientsWithAppointmentsToday(long count) {
            stats.patientsWithAppointmentsToday = count;
            return this;
        }

        public Builder patientsWithPendingInvoices(long count) {
            stats.patientsWithPendingInvoices = count;
            return this;
        }

        public Builder totalOutstandingBalance(double balance) {
            stats.totalOutstandingBalance = balance;
            return this;
        }

        public PatientStatistics build() {
            return stats;
        }
    }

    // Getters and Setters
    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getActivePatients() {
        return activePatients;
    }

    public void setActivePatients(long activePatients) {
        this.activePatients = activePatients;
    }

    public long getInactivePatients() {
        return inactivePatients;
    }

    public void setInactivePatients(long inactivePatients) {
        this.inactivePatients = inactivePatients;
    }

    public long getNewPatientsThisMonth() {
        return newPatientsThisMonth;
    }

    public void setNewPatientsThisMonth(long newPatientsThisMonth) {
        this.newPatientsThisMonth = newPatientsThisMonth;
    }

    public long getMalePatients() {
        return malePatients;
    }

    public void setMalePatients(long malePatients) {
        this.malePatients = malePatients;
    }

    public long getFemalePatients() {
        return femalePatients;
    }

    public void setFemalePatients(long femalePatients) {
        this.femalePatients = femalePatients;
    }

    public double getAverageAge() {
        return averageAge;
    }

    public void setAverageAge(double averageAge) {
        this.averageAge = averageAge;
    }

    public long getPatientsWithAppointmentsToday() {
        return patientsWithAppointmentsToday;
    }

    public void setPatientsWithAppointmentsToday(long patientsWithAppointmentsToday) {
        this.patientsWithAppointmentsToday = patientsWithAppointmentsToday;
    }

    public long getPatientsWithPendingInvoices() {
        return patientsWithPendingInvoices;
    }

    public void setPatientsWithPendingInvoices(long patientsWithPendingInvoices) {
        this.patientsWithPendingInvoices = patientsWithPendingInvoices;
    }

    public double getTotalOutstandingBalance() {
        return totalOutstandingBalance;
    }

    public void setTotalOutstandingBalance(double totalOutstandingBalance) {
        this.totalOutstandingBalance = totalOutstandingBalance;
    }

    public Long getoPositiveCount() {
        return oPositiveCount;
    }

    public void setoPositiveCount(Long oPositiveCount) {
        this.oPositiveCount = oPositiveCount;
    }

    public Long getoNegativeCount() {
        return oNegativeCount;
    }

    public void setoNegativeCount(Long oNegativeCount) {
        this.oNegativeCount = oNegativeCount;
    }

    public Long getaPositiveCount() {
        return aPositiveCount;
    }

    public void setaPositiveCount(Long aPositiveCount) {
        this.aPositiveCount = aPositiveCount;
    }

    public Long getaNegativeCount() {
        return aNegativeCount;
    }

    public void setaNegativeCount(Long aNegativeCount) {
        this.aNegativeCount = aNegativeCount;
    }

    public Long getbPositiveCount() {
        return bPositiveCount;
    }

    public void setbPositiveCount(Long bPositiveCount) {
        this.bPositiveCount = bPositiveCount;
    }

    public Long getbNegativeCount() {
        return bNegativeCount;
    }

    public void setbNegativeCount(Long bNegativeCount) {
        this.bNegativeCount = bNegativeCount;
    }

    public Long getAbPositiveCount() {
        return abPositiveCount;
    }

    public void setAbPositiveCount(Long abPositiveCount) {
        this.abPositiveCount = abPositiveCount;
    }

    public Long getAbNegativeCount() {
        return abNegativeCount;
    }

    public void setAbNegativeCount(Long abNegativeCount) {
        this.abNegativeCount = abNegativeCount;
    }

    // Utility methods
    public double getGenderRatio() {
        if (femalePatients == 0) return malePatients > 0 ? Double.POSITIVE_INFINITY : 0;
        return (double) malePatients / femalePatients;
    }

    public double getActivePercentage() {
        if (totalPatients == 0) return 0;
        return (double) activePatients / totalPatients * 100;
    }

    public double getInactivePercentage() {
        if (totalPatients == 0) return 0;
        return (double) inactivePatients / totalPatients * 100;
    }

    @Override
    public String toString() {
        return "PatientStatistics{" +
                "totalPatients=" + totalPatients +
                ", activePatients=" + activePatients +
                ", inactivePatients=" + inactivePatients +
                ", newPatientsThisMonth=" + newPatientsThisMonth +
                ", malePatients=" + malePatients +
                ", femalePatients=" + femalePatients +
                ", averageAge=" + averageAge +
                ", patientsWithAppointmentsToday=" + patientsWithAppointmentsToday +
                ", patientsWithPendingInvoices=" + patientsWithPendingInvoices +
                ", totalOutstandingBalance=" + totalOutstandingBalance +
                '}';
    }
}