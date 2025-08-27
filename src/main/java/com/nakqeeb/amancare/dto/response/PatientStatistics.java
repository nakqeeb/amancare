// =============================================================================
// Patient Statistics DTO - إحصائيات المرضى
// =============================================================================

package com.nakqeeb.amancare.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * إحصائيات المرضى
 */
@Schema(description = "إحصائيات المرضى")
public class PatientStatistics {

    @Schema(description = "إجمالي عدد المرضى النشطين", example = "150")
    private long totalActivePatients;

    @Schema(description = "عدد المرضى الجدد هذا الشهر", example = "12")
    private long newPatientsThisMonth;

    // Constructors
    public PatientStatistics() {}

    public PatientStatistics(long totalActivePatients, long newPatientsThisMonth) {
        this.totalActivePatients = totalActivePatients;
        this.newPatientsThisMonth = newPatientsThisMonth;
    }

    // Getters and Setters
    public long getTotalActivePatients() { return totalActivePatients; }
    public void setTotalActivePatients(long totalActivePatients) { this.totalActivePatients = totalActivePatients; }

    public long getNewPatientsThisMonth() { return newPatientsThisMonth; }
    public void setNewPatientsThisMonth(long newPatientsThisMonth) { this.newPatientsThisMonth = newPatientsThisMonth; }
}
