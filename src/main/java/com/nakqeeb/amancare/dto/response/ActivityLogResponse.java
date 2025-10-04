package com.nakqeeb.amancare.dto.response;

import com.nakqeeb.amancare.entity.ActivityLog;
import com.nakqeeb.amancare.entity.ActionType;

import java.time.LocalDateTime;

/**
 * استجابة سجل النشاط
 * Activity Log Response DTO
 */
public class ActivityLogResponse {

    private Long id;
    private Long clinicId;
    private String clinicName;
    private Long userId;
    private String username;
    private String userFullName;
    private String userRole;
    private ActionType actionType;
    private String httpMethod;
    private String entityType;
    private Long entityId;
    private String entityName;
    private String description;
    private String endpoint;
    private String ipAddress;
    private LocalDateTime timestamp;
    private Boolean success;
    private String errorMessage;
    private Long durationMs;

    // =============================================================================
    // CONSTRUCTORS
    // =============================================================================

    public ActivityLogResponse() {
    }

    // =============================================================================
    // FACTORY METHOD
    // =============================================================================

    /**
     * Convert ActivityLog entity to Response DTO
     */
    public static ActivityLogResponse fromEntity(ActivityLog activity) {
        ActivityLogResponse response = new ActivityLogResponse();
        response.id = activity.getId();
        response.clinicId = activity.getClinicId();
        response.clinicName = activity.getClinicName();
        response.userId = activity.getUserId();
        response.username = activity.getUsername();
        response.userFullName = activity.getUserFullName();
        response.userRole = activity.getUserRole();
        response.actionType = activity.getActionType();
        response.httpMethod = activity.getHttpMethod();
        response.entityType = activity.getEntityType();
        response.entityId = activity.getEntityId();
        response.entityName = activity.getEntityName();
        response.description = activity.getDescription();
        response.endpoint = activity.getEndpoint();
        response.ipAddress = activity.getIpAddress();
        response.timestamp = activity.getTimestamp();
        response.success = activity.getSuccess();
        response.errorMessage = activity.getErrorMessage();
        response.durationMs = activity.getDurationMs();
        return response;
    }

    // =============================================================================
    // GETTERS AND SETTERS
    // =============================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClinicId() { return clinicId; }
    public void setClinicId(Long clinicId) { this.clinicId = clinicId; }

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
}