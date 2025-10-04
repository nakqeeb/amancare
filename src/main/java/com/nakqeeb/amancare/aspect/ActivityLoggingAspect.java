// =============================================================================
// Activity Logging Aspect - جانب تسجيل الأنشطة
// src/main/java/com/nakqeeb/amancare/aspect/ActivityLoggingAspect.java
// =============================================================================

package com.nakqeeb.amancare.aspect;

import com.nakqeeb.amancare.entity.ActionType;
import com.nakqeeb.amancare.security.UserPrincipal;
import com.nakqeeb.amancare.service.ActivityLogService;
import com.nakqeeb.amancare.service.ClinicContextService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

/**
 * جانب تسجيل الأنشطة - يعترض جميع طلبات POST, PUT, PATCH, DELETE
 * Activity Logging Aspect - Intercepts all POST, PUT, PATCH, DELETE requests
 */
@Aspect
@Component
public class ActivityLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLoggingAspect.class);

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ClinicContextService clinicContextService;

    // =============================================================================
    // POINTCUT: All Controller Methods with Write Operations
    // =============================================================================

    /**
     * Intercepts all controller methods annotated with @PostMapping, @PutMapping,
     * @PatchMapping, or @DeleteMapping
     */
    @Around(
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.DeleteMapping)) && " +
                    "within(com.nakqeeb.amancare.controller..*)"
    )
    public Object logActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Get request details
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            return joinPoint.proceed(); // No request context, just proceed
        }

        // Get authenticated user
        UserPrincipal currentUser = getCurrentUser();
        if (currentUser == null) {
            return joinPoint.proceed(); // Not authenticated, just proceed
        }

        // Get clinic ID
        Long clinicId = getClinicId(currentUser);
        if (clinicId == null) {
            return joinPoint.proceed(); // No clinic context, just proceed
        }

        // Get method details
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Determine action type from HTTP method
        ActionType actionType = determineActionType(method);
        String httpMethod = actionType.name();

        // Get endpoint path
        String endpoint = request.getRequestURI();

        // Extract entity details from method name and parameters
        EntityDetails entityDetails = extractEntityDetails(method, joinPoint.getArgs());

        // Execute the actual method
        Object result = null;
        boolean success = true;
        String errorMessage = null;

        try {
            result = joinPoint.proceed();

            // Edited: Special handling for authentication methods
            if (entityDetails.isAuthenticationMethod) {
                // Extract user info from authentication response
                if (result != null) {
                    entityDetails.entityId = extractUserIdFromAuthResponse(result);
                    entityDetails.entityName = extractUserNameFromAuthResponse(result);
                }
            } else {
                // Normal entity handling
                // For successful operations, extract entity ID from result
                // This is especially important for CREATE operations
                if (result != null && entityDetails.entityId == null) {
                    entityDetails.entityId = extractEntityIdFromResult(result);

                    // Also try to extract entity name if not already set
                    if (entityDetails.entityName == null) {
                        entityDetails.entityName = extractEntityNameFromResult(result);
                    }

                    // Debug logging for CREATE operations
                    if (actionType == ActionType.CREATE) {
                        logger.debug("CREATE operation - Extracted entityId: {}, entityName: {} for entityType: {}",
                                entityDetails.entityId, entityDetails.entityName, entityDetails.entityType);
                    }
                }
            }
            // End of edit

        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            throw e; // Re-throw to maintain normal error handling
        } finally {
            // Calculate duration
            long duration = System.currentTimeMillis() - startTime;

            // Get IP address and User Agent
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            // Build description
            String description = buildDescription(
                    actionType,
                    entityDetails.entityType,
                    entityDetails.entityName,
                    currentUser.getFullName()
            );

            // Log activity asynchronously
            activityLogService.logActivity(
                    clinicId,
                    currentUser.getId(),
                    actionType,
                    httpMethod,
                    entityDetails.entityType,
                    entityDetails.entityId,
                    entityDetails.entityName,
                    description,
                    endpoint,
                    ipAddress,
                    userAgent,
                    result,
                    null, // Old value - would need additional tracking
                    success,
                    errorMessage,
                    duration
            );
        }

        return result;
    }

    // =============================================================================
    // HELPER METHODS
    // =============================================================================

    /**
     * Get HTTP servlet request from context
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            logger.warn("Failed to get HttpServletRequest: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get current authenticated user
     */
    private UserPrincipal getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                return (UserPrincipal) authentication.getPrincipal();
            }
        } catch (Exception e) {
            logger.warn("Failed to get current user: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get clinic ID from user context
     */
    private Long getClinicId(UserPrincipal user) {
        try {
            // For SYSTEM_ADMIN, get from context service
            if ("SYSTEM_ADMIN".equals(user.getRole())) {
                return clinicContextService.getEffectiveClinicId(user);
            }
            // For other users, get from their profile
            return user.getClinicId();
        } catch (Exception e) {
            logger.warn("Failed to get clinic ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Determine action type from method annotations
     */
    private ActionType determineActionType(Method method) {
        if (method.isAnnotationPresent(PostMapping.class)) {
            return ActionType.CREATE;
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return ActionType.UPDATE;
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            return ActionType.PATCH;
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return ActionType.DELETE;
        }
        return ActionType.UPDATE; // Default fallback
    }

    /**
     * Extract entity details from method name and parameters
     */
    private EntityDetails extractEntityDetails(Method method, Object[] args) {
        EntityDetails details = new EntityDetails();

        String methodName = method.getName();

        // Edited: Added special handling for authentication methods
        if (methodName.contains("login") || methodName.contains("Login")) {
            details.entityType = "Authentication";
            details.isAuthenticationMethod = true;
            return details; // Don't process further for login
        } else if (methodName.contains("logout") || methodName.contains("Logout")) {
            details.entityType = "Authentication";
            details.isAuthenticationMethod = true;
            return details;
        } else if (methodName.contains("register") || methodName.contains("Register")) {
            details.entityType = "Registration";
            details.isAuthenticationMethod = true;
            return details;
        }
        // End of edit

        // Extract entity type from method name
        if (methodName.contains("Patient")) {
            details.entityType = "Patient";
        } else if (methodName.contains("Appointment")) {
            details.entityType = "Appointment";
        } else if (methodName.contains("MedicalRecord")) {
            details.entityType = "MedicalRecord";
        } else if (methodName.contains("Invoice")) {
            details.entityType = "Invoice";
        } else if (methodName.contains("Payment")) {
            details.entityType = "Payment";
        } else if (methodName.contains("User")) {
            details.entityType = "User";
        } else if (methodName.contains("Clinic")) {
            details.entityType = "Clinic";
        } else {
            // Try to extract from class name
            String className = method.getDeclaringClass().getSimpleName();
            details.entityType = className.replace("Controller", "");
        }

        // Try to extract entity ID from path variables
        for (Object arg : args) {
            if (arg instanceof Long) {
                details.entityId = (Long) arg;
                break;
            }
        }

        return details;
    }

    /**
     * Extract entity ID from result (for POST operations)
     */
    private Long extractEntityIdFromResult(Object result) {
        try {
            if (result instanceof ResponseEntity) {
                Object body = ((ResponseEntity<?>) result).getBody();
                if (body != null) {
                    return extractIdFromObject(body);
                }
            } else {
                return extractIdFromObject(result);
            }
        } catch (Exception e) {
            logger.debug("Could not extract entity ID from result: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract ID from an object using reflection
     */
    private Long extractIdFromObject(Object obj) {
        if (obj == null) return null;

        try {
            // First try: Check if it's an ApiResponse wrapper
            Class<?> clazz = obj.getClass();
            if (clazz.getSimpleName().contains("ApiResponse")) {
                // Try to get data field
                Method getDataMethod = clazz.getMethod("getData");
                Object data = getDataMethod.invoke(obj);
                if (data != null) {
                    return extractIdFromObject(data); // Recursive call
                }
            }

            // Second try: Direct getId() method
            try {
                Method getIdMethod = obj.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(obj);
                if (id instanceof Long) {
                    return (Long) id;
                } else if (id instanceof Integer) {
                    return ((Integer) id).longValue();
                } else if (id instanceof String) {
                    return Long.parseLong((String) id);
                }
            } catch (NoSuchMethodException e) {
                // Try 'id' field directly
                try {
                    java.lang.reflect.Field idField = obj.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object id = idField.get(obj);
                    if (id instanceof Long) {
                        return (Long) id;
                    } else if (id instanceof Integer) {
                        return ((Integer) id).longValue();
                    }
                } catch (Exception ignored) {
                    // No id field
                }
            }
        } catch (Exception e) {
            logger.debug("Error extracting ID: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract entity name from result (for POST operations)
     */
    private String extractEntityNameFromResult(Object result) {
        try {
            if (result instanceof ResponseEntity) {
                Object body = ((ResponseEntity<?>) result).getBody();
                if (body != null) {
                    return extractNameFromObject(body);
                }
            } else {
                return extractNameFromObject(result);
            }
        } catch (Exception e) {
            logger.debug("Could not extract entity name from result: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract name from an object using reflection
     */
    private String extractNameFromObject(Object obj) {
        if (obj == null) return null;

        try {
            // Check if it's an ApiResponse wrapper
            Class<?> clazz = obj.getClass();
            if (clazz.getSimpleName().contains("ApiResponse")) {
                Method getDataMethod = clazz.getMethod("getData");
                Object data = getDataMethod.invoke(obj);
                if (data != null) {
                    return extractNameFromObject(data);
                }
            }

            // Try common name field methods
            String[] nameMethods = {"getName", "getFullName", "getFirstName", "getTitle", "getDescription"};
            for (String methodName : nameMethods) {
                try {
                    Method method = obj.getClass().getMethod(methodName);
                    Object name = method.invoke(obj);
                    if (name instanceof String && !((String) name).isEmpty()) {
                        // If it's firstName, try to append lastName
                        if (methodName.equals("getFirstName")) {
                            try {
                                Method lastNameMethod = obj.getClass().getMethod("getLastName");
                                Object lastName = lastNameMethod.invoke(obj);
                                if (lastName instanceof String) {
                                    return name + " " + lastName;
                                }
                            } catch (Exception ignored) {
                                // No lastName, just return firstName
                            }
                        }
                        return (String) name;
                    }
                } catch (NoSuchMethodException ignored) {
                    // Try next method
                }
            }
        } catch (Exception e) {
            logger.debug("Error extracting name: {}", e.getMessage());
        }
        return null;
    }

    // Edited: Added methods to extract user info from authentication responses
    /**
     * Extract user ID from authentication response
     */
    private Long extractUserIdFromAuthResponse(Object result) {
        try {
            if (result instanceof ResponseEntity) {
                Object body = ((ResponseEntity<?>) result).getBody();
                if (body != null) {
                    return extractUserIdFromAuthObject(body);
                }
            } else {
                return extractUserIdFromAuthObject(result);
            }
        } catch (Exception e) {
            logger.debug("Could not extract user ID from auth response: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract user ID from authentication object
     */
    private Long extractUserIdFromAuthObject(Object obj) {
        if (obj == null) return null;

        try {
            // Check if it's an ApiResponse wrapper
            Class<?> clazz = obj.getClass();
            if (clazz.getSimpleName().contains("ApiResponse")) {
                Method getDataMethod = clazz.getMethod("getData");
                Object data = getDataMethod.invoke(obj);
                if (data != null) {
                    return extractUserIdFromAuthObject(data);
                }
            }

            // Check for JwtAuthenticationResponse or similar
            // Try to get userId or id
            String[] idMethods = {"getUserId", "getId"};
            for (String methodName : idMethods) {
                try {
                    Method method = obj.getClass().getMethod(methodName);
                    Object id = method.invoke(obj);
                    if (id instanceof Long) {
                        return (Long) id;
                    } else if (id instanceof Integer) {
                        return ((Integer) id).longValue();
                    }
                } catch (NoSuchMethodException ignored) {
                    // Try next method
                }
            }
        } catch (Exception e) {
            logger.debug("Error extracting user ID from auth object: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract user name from authentication response
     */
    private String extractUserNameFromAuthResponse(Object result) {
        try {
            if (result instanceof ResponseEntity) {
                Object body = ((ResponseEntity<?>) result).getBody();
                if (body != null) {
                    return extractUserNameFromAuthObject(body);
                }
            } else {
                return extractUserNameFromAuthObject(result);
            }
        } catch (Exception e) {
            logger.debug("Could not extract user name from auth response: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract user name from authentication object
     */
    private String extractUserNameFromAuthObject(Object obj) {
        if (obj == null) return null;

        try {
            // Check if it's an ApiResponse wrapper
            Class<?> clazz = obj.getClass();
            if (clazz.getSimpleName().contains("ApiResponse")) {
                Method getDataMethod = clazz.getMethod("getData");
                Object data = getDataMethod.invoke(obj);
                if (data != null) {
                    return extractUserNameFromAuthObject(data);
                }
            }

            // Try to get full name, firstName + lastName, or username
            String[] nameMethods = {"getFullName", "getFirstName", "getUsername"};
            for (String methodName : nameMethods) {
                try {
                    Method method = obj.getClass().getMethod(methodName);
                    Object name = method.invoke(obj);
                    if (name instanceof String && !((String) name).isEmpty()) {
                        // If it's firstName, try to append lastName
                        if (methodName.equals("getFirstName")) {
                            try {
                                Method lastNameMethod = obj.getClass().getMethod("getLastName");
                                Object lastName = lastNameMethod.invoke(obj);
                                if (lastName instanceof String && !((String) lastName).isEmpty()) {
                                    return name + " " + lastName;
                                }
                            } catch (Exception ignored) {
                                // No lastName
                            }
                        }
                        return (String) name;
                    }
                } catch (NoSuchMethodException ignored) {
                    // Try next method
                }
            }
        } catch (Exception e) {
            logger.debug("Error extracting user name from auth object: {}", e.getMessage());
        }
        return null;
    }
    // End of edit

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
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

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Get first IP if there are multiple (proxy chain)
                int index = ip.indexOf(',');
                if (index != -1) {
                    ip = ip.substring(0, index);
                }
                return ip.trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Build human-readable description
     */
    private String buildDescription(
            ActionType actionType,
            String entityType,
            String entityName,
            String userName
    ) {
        // Edited: Special handling for authentication methods
        if ("Authentication".equals(entityType)) {
            if (entityName != null && !entityName.isEmpty()) {
                return String.format("تم تسجيل دخول %s", entityName);
            } else if (userName != null && !userName.isEmpty()) {
                return String.format("تم تسجيل دخول %s", userName);
            } else {
                return "تم تسجيل دخول مستخدم";
            }
        } else if ("Registration".equals(entityType)) {
            if (entityName != null && !entityName.isEmpty()) {
                return String.format("تم تسجيل حساب جديد: %s", entityName);
            } else {
                return "تم تسجيل حساب جديد";
            }
        }
        // End of edit

        String action;
        switch (actionType) {
            case CREATE:
                action = "أضاف";
                break;
            case UPDATE:
            case PATCH:
                action = "حدّث";
                break;
            case DELETE:
                action = "حذف";
                break;
            default:
                action = "عدّل";
        }

        String entity = translateEntityType(entityType);

        if (entityName != null && !entityName.isEmpty()) {
            return String.format("%s %s: %s", action, entity, entityName);
        } else {
            return String.format("%s %s", action, entity);
        }
    }

    /**
     * Translate entity type to Arabic
     */
    private String translateEntityType(String entityType) {
        switch (entityType) {
            case "Patient":
                return "مريض";
            case "Appointment":
                return "موعد";
            case "MedicalRecord":
                return "سجل طبي";
            case "Invoice":
                return "فاتورة";
            case "Payment":
                return "دفعة";
            case "User":
                return "مستخدم";
            case "Clinic":
                return "عيادة";
            // Edited: Added authentication types
            case "Authentication":
                return "تسجيل دخول";
            case "Registration":
                return "تسجيل حساب";
            // End of edit
            default:
                return entityType;
        }
    }

    // =============================================================================
    // INNER CLASSES
    // =============================================================================

    /**
     * Entity details extracted from method
     */
    private static class EntityDetails {
        String entityType;
        Long entityId;
        String entityName;
        boolean isAuthenticationMethod = false; // Edited: Added flag for auth methods
    }
}