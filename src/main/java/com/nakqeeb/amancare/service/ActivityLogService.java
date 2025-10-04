// =============================================================================
// Activity Log Service - خدمة سجلات الأنشطة
// src/main/java/com/nakqeeb/amancare/service/ActivityLogService.java
// =============================================================================

package com.nakqeeb.amancare.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nakqeeb.amancare.dto.response.ActivityLogResponse;
import com.nakqeeb.amancare.dto.response.ActivityStatisticsResponse;
import com.nakqeeb.amancare.entity.ActivityLog;
import com.nakqeeb.amancare.entity.ActionType;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.repository.ActivityLogRepository;
import com.nakqeeb.amancare.repository.UserRepository;
import com.nakqeeb.amancare.repository.ClinicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * خدمة سجلات الأنشطة
 * Service for managing activity logs
 */
@Service
@Transactional
public class ActivityLogService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogService.class);

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // =============================================================================
    // CREATE ACTIVITY LOG (Async for performance)
    // =============================================================================

    /**
     * Log an activity asynchronously to avoid blocking the main request thread
     */
    @Async
    public void logActivity(
            Long clinicId,
            Long userId,
            ActionType actionType,
            String httpMethod,
            String entityType,
            Long entityId,
            String entityName,
            String description,
            String endpoint,
            String ipAddress,
            String userAgent,
            Object newValue,
            Object oldValue,
            Boolean success,
            String errorMessage,
            Long durationMs
    ) {
        try {
            // Get user details
            User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
            Clinic clinic = clinicId != null ? clinicRepository.findById(clinicId).orElse(null) : null;

            // Build activity log
            ActivityLog activity = ActivityLog.builder()
                    .clinicId(clinicId)
                    .clinicName(clinic != null ? clinic.getName() : null)
                    .userId(userId)
                    .username(user != null ? user.getUsername() : "Unknown")
                    .userFullName(user != null ? user.getFullName() : "Unknown User")
                    .userRole(user != null ? user.getRole().name() : null)
                    .actionType(actionType)
                    .httpMethod(httpMethod)
                    .entityType(entityType)
                    .entityId(entityId)
                    .entityName(entityName)
                    .description(description)
                    .endpoint(endpoint)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(success)
                    .errorMessage(errorMessage)
                    .durationMs(durationMs)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Convert values to JSON if provided
            if (newValue != null) {
                try {
                    activity.setNewValue(objectMapper.writeValueAsString(newValue));
                } catch (JsonProcessingException e) {
                    logger.warn("Failed to serialize new value: {}", e.getMessage());
                }
            }

            if (oldValue != null) {
                try {
                    activity.setOldValue(objectMapper.writeValueAsString(oldValue));
                } catch (JsonProcessingException e) {
                    logger.warn("Failed to serialize old value: {}", e.getMessage());
                }
            }

            // Save activity log
            activityLogRepository.save(activity);

            logger.debug("Activity logged: {} {} by user {} in clinic {}",
                    actionType, entityType, userId, clinicId);

        } catch (Exception e) {
            // Never throw exceptions from logging to avoid breaking the main request
            logger.error("Failed to log activity: {}", e.getMessage(), e);
        }
    }

    /**
     * Simplified logging method for common use cases
     */
    @Async
    public void logSimpleActivity(
            Long clinicId,
            Long userId,
            ActionType actionType,
            String entityType,
            Long entityId,
            String entityName,
            String description
    ) {
        logActivity(
                clinicId, userId, actionType,
                actionType.name(), entityType, entityId, entityName,
                description, null, null, null, null, null,
                true, null, null
        );
    }

    // =============================================================================
    // QUERY ACTIVITIES
    // =============================================================================

    /**
     * Get recent activities for a clinic
     */
    public List<ActivityLogResponse> getRecentActivities(Long clinicId, int limit) {
        List<ActivityLog> activities = limit <= 50
                ? activityLogRepository.findTop50ByClinicIdOrderByTimestampDesc(clinicId)
                : activityLogRepository.findByClinicIdOrderByTimestampDesc(
                clinicId, PageRequest.of(0, limit)).getContent();

        return activities.stream()
                .map(ActivityLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Search activities with filters
     */
    public Page<ActivityLogResponse> searchActivities(
            Long clinicId,
            Long userId,
            ActionType actionType,
            String entityType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String searchTerm,
            Pageable pageable
    ) {
        Page<ActivityLog> activities = activityLogRepository.searchActivities(
                clinicId, userId, actionType, entityType,
                startDate, endDate, searchTerm, pageable
        );

        return activities.map(ActivityLogResponse::fromEntity);
    }

    /**
     * Get activity trail for a specific entity
     */
    public List<ActivityLogResponse> getEntityActivityTrail(
            Long clinicId,
            String entityType,
            Long entityId
    ) {
        List<ActivityLog> activities = activityLogRepository.getEntityActivityTrail(
                clinicId, entityType, entityId
        );

        return activities.stream()
                .map(ActivityLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get activities by user
     */
    public Page<ActivityLogResponse> getActivitiesByUser(
            Long clinicId,
            Long userId,
            Pageable pageable
    ) {
        Page<ActivityLog> activities = activityLogRepository
                .findByClinicIdAndUserIdOrderByTimestampDesc(clinicId, userId, pageable);

        return activities.map(ActivityLogResponse::fromEntity);
    }

    /**
     * Get activities within date range
     */
    public Page<ActivityLogResponse> getActivitiesByDateRange(
            Long clinicId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        Page<ActivityLog> activities = activityLogRepository
                .findByClinicIdAndTimestampBetweenOrderByTimestampDesc(
                        clinicId, startDate, endDate, pageable);

        return activities.map(ActivityLogResponse::fromEntity);
    }

    // =============================================================================
    // STATISTICS
    // =============================================================================

    /**
     * Get activity statistics for a clinic
     */
    public ActivityStatisticsResponse getActivityStatistics(Long clinicId, LocalDateTime since) {
        ActivityStatisticsResponse stats = new ActivityStatisticsResponse();

        // Total activities
        LocalDateTime endDate = LocalDateTime.now();
        stats.setTotalActivities(
                activityLogRepository.countByClinicIdAndTimestampBetween(clinicId, since, endDate)
        );

        // Action type breakdown
        Map<String, Long> actionTypeMap = new HashMap<>();
        List<Object[]> actionTypeCounts = activityLogRepository.countByActionType(clinicId, since);
        for (Object[] row : actionTypeCounts) {
            ActionType actionType = (ActionType) row[0];
            Long count = (Long) row[1];
            actionTypeMap.put(actionType.name(), count);
        }
        stats.setActionTypeBreakdown(actionTypeMap);

        // Entity type breakdown
        Map<String, Long> entityTypeMap = new HashMap<>();
        List<Object[]> entityTypeCounts = activityLogRepository.countByEntityType(clinicId, since);
        for (Object[] row : entityTypeCounts) {
            String entityType = (String) row[0];
            Long count = (Long) row[1];
            if (entityType != null) {
                entityTypeMap.put(entityType, count);
            }
        }
        stats.setEntityTypeBreakdown(entityTypeMap);

        // User activity breakdown
        Map<String, Long> userActivityMap = new HashMap<>();
        List<Object[]> userCounts = activityLogRepository.countByUser(clinicId, since);
        int uniqueUsers = 0;
        for (Object[] row : userCounts) {
            Long userId = (Long) row[0];
            String userFullName = (String) row[1];
            Long count = (Long) row[2];
            userActivityMap.put(userFullName != null ? userFullName : "User " + userId, count);
            uniqueUsers++;
        }
        stats.setUserActivityBreakdown(userActivityMap);
        stats.setUniqueUsers((long) uniqueUsers);

        // Daily activity count
        Map<String, Long> dailyCountMap = new HashMap<>();
        List<Object[]> dailyCounts = activityLogRepository.getDailyActivityCount(clinicId, since);
        for (Object[] row : dailyCounts) {
            // Handle both java.sql.Date and java.time.LocalDate
            LocalDate date;
            if (row[0] instanceof java.sql.Date) {
                date = ((java.sql.Date) row[0]).toLocalDate();
            } else if (row[0] instanceof LocalDate) {
                date = (LocalDate) row[0];
            } else {
                continue; // Skip if unexpected type
            }
            Long count = (Long) row[1];
            dailyCountMap.put(date.toString(), count);
        }
        stats.setDailyActivityCount(dailyCountMap);

        // Activities today
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        stats.setActivitiesToday(
                activityLogRepository.countByClinicIdAndTimestampBetween(
                        clinicId, startOfToday, endDate)
        );

        // Activities this week
        LocalDateTime startOfWeek = LocalDateTime.now().minusWeeks(1);
        stats.setActivitiesThisWeek(
                activityLogRepository.countByClinicIdAndTimestampBetween(
                        clinicId, startOfWeek, endDate)
        );

        // Activities this month
        LocalDateTime startOfMonth = LocalDateTime.now().minusMonths(1);
        stats.setActivitiesThisMonth(
                activityLogRepository.countByClinicIdAndTimestampBetween(
                        clinicId, startOfMonth, endDate)
        );

        return stats;
    }

    // =============================================================================
    // MAINTENANCE
    // =============================================================================

    /**
     * Delete old activity logs (should be run periodically)
     */
    @Transactional
    public void cleanupOldLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        activityLogRepository.deleteOldLogs(cutoffDate);
        logger.info("Cleaned up activity logs older than {} days", retentionDays);
    }

    /**
     * Delete all activity logs for a clinic
     */
    @Transactional
    public void deleteClinicActivities(Long clinicId) {
        activityLogRepository.deleteByClinicId(clinicId);
        logger.info("Deleted all activity logs for clinic {}", clinicId);
    }
}