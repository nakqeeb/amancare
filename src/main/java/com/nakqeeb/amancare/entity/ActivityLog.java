package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * سجل الأنشطة - يسجل جميع عمليات POST, PUT, PATCH, DELETE لكل عيادة
 * Activity Log - Tracks all POST, PUT, PATCH, DELETE operations per clinic
 */
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_clinic", columnList = "clinic_id"),
        @Index(name = "idx_activity_user", columnList = "user_id"),
        @Index(name = "idx_activity_timestamp", columnList = "timestamp"),
        @Index(name = "idx_activity_action", columnList = "action_type"),
        @Index(name = "idx_activity_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_activity_clinic_timestamp", columnList = "clinic_id, timestamp")
})
public class ActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =============================================================================
    // CLINIC & USER INFORMATION
    // =============================================================================

    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;

    @Column(name = "clinic_name", length = 255)
    private String clinicName;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "user_full_name", length = 200)
    private String userFullName;

    @Column(name = "user_role", length = 50)
    private String userRole;

    // =============================================================================
    // ACTION DETAILS
    // =============================================================================

    @Column(name = "action_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_name", length = 255)
    private String entityName;

    @Column(name = "description", length = 500)
    private String description;

    // =============================================================================
    // REQUEST INFORMATION
    // =============================================================================

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // =============================================================================
    // CHANGE DETAILS
    // =============================================================================

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // JSON of old data for updates

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // JSON of new data

    // =============================================================================
    // METADATA
    // =============================================================================

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "success", nullable = false)
    private Boolean success = true;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "duration_ms")
    private Long durationMs; // Request processing time

    // =============================================================================
    // CONSTRUCTORS
    // =============================================================================

    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }

    // =============================================================================
    // BUILDER PATTERN
    // =============================================================================

    public static class Builder {
        private ActivityLog activity = new ActivityLog();

        public Builder clinicId(Long clinicId) {
            activity.clinicId = clinicId;
            return this;
        }

        public Builder clinicName(String clinicName) {
            activity.clinicName = clinicName;
            return this;
        }

        public Builder userId(Long userId) {
            activity.userId = userId;
            return this;
        }

        public Builder username(String username) {
            activity.username = username;
            return this;
        }

        public Builder userFullName(String userFullName) {
            activity.userFullName = userFullName;
            return this;
        }

        public Builder userRole(String userRole) {
            activity.userRole = userRole;
            return this;
        }

        public Builder actionType(ActionType actionType) {
            activity.actionType = actionType;
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            activity.httpMethod = httpMethod;
            return this;
        }

        public Builder entityType(String entityType) {
            activity.entityType = entityType;
            return this;
        }

        public Builder entityId(Long entityId) {
            activity.entityId = entityId;
            return this;
        }

        public Builder entityName(String entityName) {
            activity.entityName = entityName;
            return this;
        }

        public Builder description(String description) {
            activity.description = description;
            return this;
        }

        public Builder endpoint(String endpoint) {
            activity.endpoint = endpoint;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            activity.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            activity.userAgent = userAgent;
            return this;
        }

        public Builder oldValue(String oldValue) {
            activity.oldValue = oldValue;
            return this;
        }

        public Builder newValue(String newValue) {
            activity.newValue = newValue;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            activity.timestamp = timestamp;
            return this;
        }

        public Builder success(Boolean success) {
            activity.success = success;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            activity.errorMessage = errorMessage;
            return this;
        }

        public Builder durationMs(Long durationMs) {
            activity.durationMs = durationMs;
            return this;
        }

        public ActivityLog build() {
            if (activity.timestamp == null) {
                activity.timestamp = LocalDateTime.now();
            }
            return activity;
        }
    }

    public static Builder builder() {
        return new Builder();
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

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
}

