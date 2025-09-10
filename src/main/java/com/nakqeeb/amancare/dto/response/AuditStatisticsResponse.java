package com.nakqeeb.amancare.dto.response;

import java.util.Map;

public class AuditStatisticsResponse {
    private Long totalActions;
    private Long uniqueAdmins;
    private Long uniqueClinics;
    private Long criticalActions;
    private Map<String, Long> actionTypeBreakdown;
    private Map<String, Long> resourceTypeBreakdown;
    private Map<String, Long> clinicBreakdown;
    private Map<String, Long> dailyActionCount;

    // Getters and Setters
    public Long getTotalActions() {
        return totalActions;
    }

    public void setTotalActions(Long totalActions) {
        this.totalActions = totalActions;
    }

    public Long getUniqueAdmins() {
        return uniqueAdmins;
    }

    public void setUniqueAdmins(Long uniqueAdmins) {
        this.uniqueAdmins = uniqueAdmins;
    }

    public Long getUniqueClinics() {
        return uniqueClinics;
    }

    public void setUniqueClinics(Long uniqueClinics) {
        this.uniqueClinics = uniqueClinics;
    }

    public Long getCriticalActions() {
        return criticalActions;
    }

    public void setCriticalActions(Long criticalActions) {
        this.criticalActions = criticalActions;
    }

    public Map<String, Long> getActionTypeBreakdown() {
        return actionTypeBreakdown;
    }

    public void setActionTypeBreakdown(Map<String, Long> actionTypeBreakdown) {
        this.actionTypeBreakdown = actionTypeBreakdown;
    }

    public Map<String, Long> getResourceTypeBreakdown() {
        return resourceTypeBreakdown;
    }

    public void setResourceTypeBreakdown(Map<String, Long> resourceTypeBreakdown) {
        this.resourceTypeBreakdown = resourceTypeBreakdown;
    }

    public Map<String, Long> getClinicBreakdown() {
        return clinicBreakdown;
    }

    public void setClinicBreakdown(Map<String, Long> clinicBreakdown) {
        this.clinicBreakdown = clinicBreakdown;
    }

    public Map<String, Long> getDailyActionCount() {
        return dailyActionCount;
    }

    public void setDailyActionCount(Map<String, Long> dailyActionCount) {
        this.dailyActionCount = dailyActionCount;
    }
}
