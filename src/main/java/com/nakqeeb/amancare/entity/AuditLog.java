package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_clinic", columnList = "clinic_id"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_action", columnList = "action")
})
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Column(name = "clinic_id")
    private Long clinicId;

    @Column(name = "clinic_name", length = 255)
    private String clinicName;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "data", columnDefinition = "TEXT")
    private String data; // JSON data about the deleted entity

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "severity", length = 20)
    @Enumerated(EnumType.STRING)
    private AuditSeverity severity = AuditSeverity.INFO;

    @Column(name = "success")
    private Boolean success = true;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    // Enum for severity levels
    public enum AuditSeverity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    // Constructors
    public AuditLog() {}

    // Builder pattern for easy creation
    public static class Builder {
        private final AuditLog auditLog = new AuditLog();

        public Builder action(String action) {
            auditLog.action = action;
            return this;
        }

        public Builder entityType(String entityType) {
            auditLog.entityType = entityType;
            return this;
        }

        public Builder entityId(Long entityId) {
            auditLog.entityId = entityId;
            return this;
        }

        public Builder userId(Long userId) {
            auditLog.userId = userId;
            return this;
        }

        public Builder username(String username) {
            auditLog.username = username;
            return this;
        }

        public Builder userRole(String userRole) {
            auditLog.userRole = userRole;
            return this;
        }

        public Builder clinicId(Long clinicId) {
            auditLog.clinicId = clinicId;
            return this;
        }

        public Builder clinicName(String clinicName) {
            auditLog.clinicName = clinicName;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            auditLog.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            auditLog.userAgent = userAgent;
            return this;
        }

        public Builder data(String data) {
            auditLog.data = data;
            return this;
        }

        public Builder description(String description) {
            auditLog.description = description;
            return this;
        }

        public Builder severity(AuditSeverity severity) {
            auditLog.severity = severity;
            return this;
        }

        public Builder success(Boolean success) {
            auditLog.success = success;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            auditLog.errorMessage = errorMessage;
            return this;
        }

        public AuditLog build() {
            auditLog.timestamp = LocalDateTime.now();
            return auditLog;
        }
    }

    // Static factory method for permanent delete audit
    public static AuditLog permanentDelete(
            Long userId, String username, String userRole,
            Long clinicId, String clinicName,
            String entityType, Long entityId,
            String entityData, String ipAddress) {

        return new Builder()
                .action("PERMANENT_DELETE_" + entityType.toUpperCase())
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .username(username)
                .userRole(userRole)
                .clinicId(clinicId)
                .clinicName(clinicName)
                .data(entityData)
                .ipAddress(ipAddress)
                .severity(AuditSeverity.CRITICAL)
                .description("Permanent deletion of " + entityType + " ID: " + entityId)
                .success(true)
                .build();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Long getClinicId() { return clinicId; }
    public void setClinicId(Long clinicId) { this.clinicId = clinicId; }

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AuditSeverity getSeverity() { return severity; }
    public void setSeverity(AuditSeverity severity) { this.severity = severity; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}