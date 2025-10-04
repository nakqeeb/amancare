package com.nakqeeb.amancare.dto.response;

import java.util.Map;

/**
 * استجابة إحصائيات الأنشطة
 * Activity Statistics Response DTO
 */
public class ActivityStatisticsResponse {

    private Long totalActivities;
    private Long uniqueUsers;
    private Map<String, Long> actionTypeBreakdown;
    private Map<String, Long> entityTypeBreakdown;
    private Map<String, Long> userActivityBreakdown;
    private Map<String, Long> dailyActivityCount;
    private Long activitiesToday;
    private Long activitiesThisWeek;
    private Long activitiesThisMonth;

    // =============================================================================
    // CONSTRUCTORS
    // =============================================================================

    public ActivityStatisticsResponse() {
    }

    // =============================================================================
    // GETTERS AND SETTERS
    // =============================================================================

    public Long getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(Long totalActivities) {
        this.totalActivities = totalActivities;
    }

    public Long getUniqueUsers() {
        return uniqueUsers;
    }

    public void setUniqueUsers(Long uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
    }

    public Map<String, Long> getActionTypeBreakdown() {
        return actionTypeBreakdown;
    }

    public void setActionTypeBreakdown(Map<String, Long> actionTypeBreakdown) {
        this.actionTypeBreakdown = actionTypeBreakdown;
    }

    public Map<String, Long> getEntityTypeBreakdown() {
        return entityTypeBreakdown;
    }

    public void setEntityTypeBreakdown(Map<String, Long> entityTypeBreakdown) {
        this.entityTypeBreakdown = entityTypeBreakdown;
    }

    public Map<String, Long> getUserActivityBreakdown() {
        return userActivityBreakdown;
    }

    public void setUserActivityBreakdown(Map<String, Long> userActivityBreakdown) {
        this.userActivityBreakdown = userActivityBreakdown;
    }

    public Map<String, Long> getDailyActivityCount() {
        return dailyActivityCount;
    }

    public void setDailyActivityCount(Map<String, Long> dailyActivityCount) {
        this.dailyActivityCount = dailyActivityCount;
    }

    public Long getActivitiesToday() {
        return activitiesToday;
    }

    public void setActivitiesToday(Long activitiesToday) {
        this.activitiesToday = activitiesToday;
    }

    public Long getActivitiesThisWeek() {
        return activitiesThisWeek;
    }

    public void setActivitiesThisWeek(Long activitiesThisWeek) {
        this.activitiesThisWeek = activitiesThisWeek;
    }

    public Long getActivitiesThisMonth() {
        return activitiesThisMonth;
    }

    public void setActivitiesThisMonth(Long activitiesThisMonth) {
        this.activitiesThisMonth = activitiesThisMonth;
    }
}
