package com.nakqeeb.amancare.dto.response;

import java.time.LocalDateTime;

public class AuditLogResponse {
    private Long id;
    private Long adminUserId;
    private String adminUsername;
    private String adminFullName;
    private String actionType;
    private Long targetClinicId;
    private String targetClinicName;
    private String targetResourceType;
    private Long targetResourceId;
    private String reason;
    private String requestPath;
    private String requestMethod;
    private String ipAddress;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminFullName() {
        return adminFullName;
    }

    public void setAdminFullName(String adminFullName) {
        this.adminFullName = adminFullName;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Long getTargetClinicId() {
        return targetClinicId;
    }

    public void setTargetClinicId(Long targetClinicId) {
        this.targetClinicId = targetClinicId;
    }

    public String getTargetClinicName() {
        return targetClinicName;
    }

    public void setTargetClinicName(String targetClinicName) {
        this.targetClinicName = targetClinicName;
    }

    public String getTargetResourceType() {
        return targetResourceType;
    }

    public void setTargetResourceType(String targetResourceType) {
        this.targetResourceType = targetResourceType;
    }

    public Long getTargetResourceId() {
        return targetResourceId;
    }

    public void setTargetResourceId(Long targetResourceId) {
        this.targetResourceId = targetResourceId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
