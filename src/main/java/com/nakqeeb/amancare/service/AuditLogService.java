// =============================================================================
// Audit Log Service - Complete Implementation
// src/main/java/com/nakqeeb/amancare/service/AuditLogService.java
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.dto.response.AuditLogResponse;
import com.nakqeeb.amancare.dto.response.AuditStatisticsResponse;
import com.nakqeeb.amancare.entity.SystemAdminAction;
import com.nakqeeb.amancare.repository.SystemAdminActionRepository;
import com.nakqeeb.amancare.repository.UserRepository;
import com.nakqeeb.amancare.repository.ClinicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing audit logs and SYSTEM_ADMIN action tracking
 * Provides comprehensive logging, querying, and reporting capabilities
 */
@Service
@Transactional
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private SystemAdminActionRepository actionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    // Action type constants
    public static final String ACTION_CONTEXT_SWITCH = "CONTEXT_SWITCH";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_PERMANENT_DELETE = "PERMANENT_DELETE";
    public static final String ACTION_REACTIVATE = "REACTIVATE";
    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_EXPORT = "EXPORT";
    public static final String ACTION_PAYMENT = "PAYMENT";
    public static final String ACTION_CANCEL = "CANCEL";
    public static final String ACTION_ACTIVATE = "ACTIVATE";
    public static final String ACTION_DEACTIVATE = "DEACTIVATE";

    // Resource type constants
    public static final String RESOURCE_PATIENT = "PATIENT";
    public static final String RESOURCE_APPOINTMENT = "APPOINTMENT";
    public static final String RESOURCE_SCHEDULE = "SCHEDULE";
    public static final String RESOURCE_MEDICAL_RECORD = "MEDICAL_RECORD";
    public static final String RESOURCE_INVOICE = "INVOICE";
    public static final String RESOURCE_USER = "USER";
    public static final String RESOURCE_CLINIC = "CLINIC";

    /**
     * Log an action performed by SYSTEM_ADMIN
     * This is the main method called by other services
     */
    @Async // Make it async to not block main operations
    public CompletableFuture<SystemAdminAction> logAction(
            Long adminUserId,
            String actionType,
            Long targetClinicId,
            String targetResourceType,
            Long targetResourceId,
            String details) {

        try {
            SystemAdminAction action = new SystemAdminAction();
            action.setAdminUserId(adminUserId);
            action.setActionType(actionType);
            action.setTargetClinicId(targetClinicId);
            action.setTargetResourceType(targetResourceType);
            action.setTargetResourceId(targetResourceId);

            // Get request context if available
            enrichActionWithRequestContext(action, details);

            action.setCreatedAt(LocalDateTime.now());

            SystemAdminAction savedAction = actionRepository.save(action);

            logger.info("Audit Log: User {} performed {} on {} #{} in clinic {}",
                    adminUserId, actionType, targetResourceType, targetResourceId, targetClinicId);

            // Check for critical actions that need immediate notification
            checkCriticalAction(savedAction);

            return CompletableFuture.completedFuture(savedAction);

        } catch (Exception e) {
            logger.error("Failed to log audit action", e);
            // Don't fail the main operation if audit logging fails
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Log a context switch action
     */
    public void logContextSwitch(Long adminUserId, Long targetClinicId, String reason) {
        logAction(
                adminUserId,
                ACTION_CONTEXT_SWITCH,
                targetClinicId,
                null,
                null,
                "Context switched to clinic. Reason: " + reason
        );
    }

    /**
     * Log a failed action attempt
     */
    public void logFailedAction(
            Long adminUserId,
            String actionType,
            Long targetClinicId,
            String failureReason) {

        try {
            SystemAdminAction action = new SystemAdminAction();
            action.setAdminUserId(adminUserId);
            action.setActionType("FAILED_" + actionType);
            action.setTargetClinicId(targetClinicId);
            action.setReason("Failed: " + failureReason);
            action.setResponseStatus(403); // Forbidden

            enrichActionWithRequestContext(action, failureReason);
            action.setCreatedAt(LocalDateTime.now());

            actionRepository.save(action);

            logger.warn("Failed Action: User {} failed to perform {} - Reason: {}",
                    adminUserId, actionType, failureReason);

        } catch (Exception e) {
            logger.error("Failed to log failed action", e);
        }
    }

    /**
     * Get audit logs with filtering and pagination
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(
            Long adminUserId,
            Long clinicId,
            String actionType,
            String resourceType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        Page<SystemAdminAction> actions;

        // Build query based on filters
        if (adminUserId != null && clinicId != null) {
            actions = actionRepository.findByAdminUserIdAndTargetClinicId(
                    adminUserId, clinicId, pageable
            );
        } else if (adminUserId != null) {
            actions = actionRepository.findByAdminUserId(adminUserId, pageable);
        } else if (clinicId != null) {
            actions = actionRepository.findByTargetClinicId(clinicId, pageable);
        } else if (startDate != null && endDate != null) {
            actions = actionRepository.findByDateRange(startDate, endDate, pageable);
        } else {
            actions = actionRepository.findAll(pageable);
        }

        return actions.map(this::convertToResponse);
    }

    /**
     * Get recent actions for a specific user
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentActions(Long adminUserId, int limit) {
        List<SystemAdminAction> actions = actionRepository
                .findTop10ByAdminUserIdOrderByCreatedAtDesc(adminUserId);

        return actions.stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get audit statistics
     */
    @Transactional(readOnly = true)
    public AuditStatisticsResponse getAuditStatistics(
            Long adminUserId,
            LocalDate startDate,
            LocalDate endDate) {

        AuditStatisticsResponse stats = new AuditStatisticsResponse();

        // Get total actions
        if (adminUserId != null) {
            List<Object[]> actionStats = actionRepository.getActionStatsByAdmin(adminUserId);
            stats.setActionTypeBreakdown(convertToActionTypeMap(actionStats));
        }

        // Calculate date range statistics
        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            Page<SystemAdminAction> actions = actionRepository.findByDateRange(
                    startDateTime, endDateTime, PageRequest.of(0, 1000)
            );

            stats.setTotalActions(actions.getTotalElements());
            stats.setUniqueAdmins(countUniqueAdmins(actions.getContent()));
            stats.setUniqueClinics(countUniqueClinics(actions.getContent()));
            stats.setCriticalActions(countCriticalActions(actions.getContent()));
        }

        return stats;
    }

    /**
     * Export audit logs to CSV
     */
    @Transactional(readOnly = true)
    public String exportAuditLogsToCsv(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        StringBuilder csv = new StringBuilder();
        csv.append("Timestamp,Admin User,Action Type,Clinic,Resource Type,Resource ID,Reason,IP Address,Status\n");

        Pageable pageable = PageRequest.of(0, 10000, Sort.by("createdAt").descending());
        Page<SystemAdminAction> actions = actionRepository.findByDateRange(
                startDate, endDate, pageable
        );

        for (SystemAdminAction action : actions) {
            csv.append(formatCsvRow(action));
            csv.append("\n");
        }

        return csv.toString();
    }

    /**
     * Get actions for specific resource
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getResourceAuditTrail(
            String resourceType,
            Long resourceId) {

        List<SystemAdminAction> actions = actionRepository
                .findByTargetResourceTypeAndTargetResourceId(resourceType, resourceId);

        return actions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Check if user has performed actions recently
     */
    @Transactional(readOnly = true)
    public boolean hasRecentActivity(Long adminUserId, int withinMinutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(withinMinutes);

        List<SystemAdminAction> recentActions = actionRepository
                .findTop10ByAdminUserIdOrderByCreatedAtDesc(adminUserId);

        return recentActions.stream()
                .anyMatch(action -> action.getCreatedAt().isAfter(threshold));
    }

    // ==================== Private Helper Methods ====================

    /**
     * Enrich action with HTTP request context
     */
    private void enrichActionWithRequestContext(SystemAdminAction action, String details) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // Set request details
            action.setRequestPath(request.getRequestURI());
            action.setRequestMethod(request.getMethod());
            action.setIpAddress(getClientIpAddress(request));
            action.setUserAgent(request.getHeader("User-Agent"));

            // Get reason from header or use provided details
            String headerReason = request.getHeader("X-Acting-Reason");
            if (headerReason != null && !headerReason.isEmpty()) {
                action.setReason(headerReason + " | " + (details != null ? details : ""));
            } else {
                action.setReason(details);
            }

            // Log request body for POST/PUT (be careful with sensitive data)
            if ("POST".equals(request.getMethod()) || "PUT".equals(request.getMethod())) {
                // Note: You might want to sanitize or limit this
                action.setRequestBody(getRequestBodyPreview(request));
            }
        } else {
            action.setReason(details);
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle comma-separated IPs (in case of proxies)
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Get request body preview (limited to prevent large data storage)
     */
    private String getRequestBodyPreview(HttpServletRequest request) {
        // This is a simplified version - in production you'd want to
        // properly read and cache the request body
        return "Request body logging enabled";
    }

    /**
     * Check if action is critical and needs immediate notification
     */
    private void checkCriticalAction(SystemAdminAction action) {
        Set<String> criticalActions = Set.of(
                ACTION_PERMANENT_DELETE,
                "FAILED_DELETE",
                "SECURITY_BREACH"
        );

        if (criticalActions.contains(action.getActionType())) {
            logger.error("CRITICAL ACTION DETECTED: {} by user {} on {} #{}",
                    action.getActionType(),
                    action.getAdminUserId(),
                    action.getTargetResourceType(),
                    action.getTargetResourceId()
            );

            // Here you could trigger notifications, emails, etc.
            notifySecurityTeam(action);
        }
    }

    /**
     * Notify security team of critical actions
     */
    private void notifySecurityTeam(SystemAdminAction action) {
        // Implementation would send email/SMS/Slack notification
        logger.info("Security team notified of critical action: {}", action.getId());
    }

    /**
     * Convert SystemAdminAction to response DTO
     */
    private AuditLogResponse convertToResponse(SystemAdminAction action) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(action.getId());
        response.setAdminUserId(action.getAdminUserId());
        response.setActionType(action.getActionType());
        response.setTargetClinicId(action.getTargetClinicId());
        response.setTargetResourceType(action.getTargetResourceType());
        response.setTargetResourceId(action.getTargetResourceId());
        response.setReason(action.getReason());
        response.setRequestPath(action.getRequestPath());
        response.setRequestMethod(action.getRequestMethod());
        response.setIpAddress(action.getIpAddress());
        response.setCreatedAt(action.getCreatedAt());

        // Enrich with user and clinic names
        enrichResponseWithNames(response, action);

        return response;
    }

    /**
     * Enrich response with user and clinic names
     */
    private void enrichResponseWithNames(AuditLogResponse response, SystemAdminAction action) {
        // Get admin user name
        userRepository.findById(action.getAdminUserId()).ifPresent(user -> {
            response.setAdminUsername(user.getUsername());
            response.setAdminFullName(user.getFullName());
        });

        // Get clinic name
        if (action.getTargetClinicId() != null) {
            clinicRepository.findById(action.getTargetClinicId()).ifPresent(clinic -> {
                response.setTargetClinicName(clinic.getName());
            });
        }
    }

    /**
     * Format action for CSV export
     */
    private String formatCsvRow(SystemAdminAction action) {
        StringBuilder row = new StringBuilder();
        row.append(action.getCreatedAt()).append(",");
        row.append(action.getAdminUserId()).append(",");
        row.append(action.getActionType()).append(",");
        row.append(action.getTargetClinicId() != null ? action.getTargetClinicId() : "").append(",");
        row.append(action.getTargetResourceType() != null ? action.getTargetResourceType() : "").append(",");
        row.append(action.getTargetResourceId() != null ? action.getTargetResourceId() : "").append(",");
        row.append(escapeForCsv(action.getReason())).append(",");
        row.append(action.getIpAddress() != null ? action.getIpAddress() : "").append(",");
        row.append(action.getResponseStatus() != null ? action.getResponseStatus() : "200");
        return row.toString();
    }

    /**
     * Escape string for CSV format
     */
    private String escapeForCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Convert action statistics to map
     */
    private Map<String, Long> convertToActionTypeMap(List<Object[]> stats) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : stats) {
            map.put((String) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    /**
     * Count unique admins in actions
     */
    private long countUniqueAdmins(List<SystemAdminAction> actions) {
        return actions.stream()
                .map(SystemAdminAction::getAdminUserId)
                .distinct()
                .count();
    }

    /**
     * Count unique clinics in actions
     */
    private long countUniqueClinics(List<SystemAdminAction> actions) {
        return actions.stream()
                .map(SystemAdminAction::getTargetClinicId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    /**
     * Count critical actions
     */
    private long countCriticalActions(List<SystemAdminAction> actions) {
        Set<String> criticalTypes = Set.of(
                ACTION_PERMANENT_DELETE,
                ACTION_DELETE,
                "FAILED_DELETE",
                "SECURITY_BREACH"
        );

        return actions.stream()
                .filter(action -> criticalTypes.contains(action.getActionType()))
                .count();
    }
}


