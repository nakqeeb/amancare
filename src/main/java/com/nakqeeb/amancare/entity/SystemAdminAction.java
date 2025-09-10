// =============================================================================
// SystemAdminAction Entity - كيان تسجيل إجراءات مدير النظام
// =============================================================================

package com.nakqeeb.amancare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * كيان لتسجيل جميع إجراءات SYSTEM_ADMIN عند التصرف نيابة عن العيادات
 */
@Entity
@Table(name = "system_admin_actions")
public class SystemAdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "target_clinic_id")
    private Long targetClinicId;

    @Column(name = "target_resource_type", length = 50)
    private String targetResourceType;

    @Column(name = "target_resource_id")
    private Long targetResourceId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "request_path")
    private String requestPath;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdminUserId() { return adminUserId; }
    public void setAdminUserId(Long adminUserId) { this.adminUserId = adminUserId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public Long getTargetClinicId() { return targetClinicId; }
    public void setTargetClinicId(Long targetClinicId) { this.targetClinicId = targetClinicId; }

    public String getTargetResourceType() { return targetResourceType; }
    public void setTargetResourceType(String targetResourceType) {
        this.targetResourceType = targetResourceType;
    }

    public Long getTargetResourceId() { return targetResourceId; }
    public void setTargetResourceId(Long targetResourceId) {
        this.targetResourceId = targetResourceId;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }

    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }

    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }

    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
