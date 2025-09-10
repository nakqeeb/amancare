// =============================================================================
// Clinic Context Service - خدمة تبديل سياق العيادة لـ SYSTEM_ADMIN
// =============================================================================

package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.entity.Clinic;
import com.nakqeeb.amancare.entity.SystemAdminAction;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.entity.UserRole;
import com.nakqeeb.amancare.exception.BadRequestException;
import com.nakqeeb.amancare.exception.ForbiddenOperationException;
import com.nakqeeb.amancare.exception.ResourceNotFoundException;
import com.nakqeeb.amancare.repository.ClinicRepository;
import com.nakqeeb.amancare.repository.SystemAdminActionRepository;
import com.nakqeeb.amancare.repository.UserRepository;
import com.nakqeeb.amancare.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * خدمة إدارة سياق العيادة لـ SYSTEM_ADMIN
 * تتيح لـ SYSTEM_ADMIN التصرف نيابة عن عيادة معينة لأغراض الدعم والصيانة
 */
@Service
@Transactional
public class ClinicContextService {

    private static final Logger logger = LoggerFactory.getLogger(ClinicContextService.class);

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SystemAdminActionRepository actionRepository;

    /**
     * الحصول على معرف العيادة الفعال للمستخدم الحالي
     *
     * @param userPrincipal المستخدم الحالي
     * @return معرف العيادة الفعال
     */
    public Long getEffectiveClinicId(UserPrincipal userPrincipal) {
        // إذا لم يكن المستخدم SYSTEM_ADMIN، ارجع معرف العيادة الخاص به
        if (!UserRole.SYSTEM_ADMIN.name().equals(userPrincipal.getRole())) {
            if (userPrincipal.getClinicId() == null) {
                throw new BadRequestException("المستخدم غير مرتبط بأي عيادة");
            }
            return userPrincipal.getClinicId();
        }

        // للـ SYSTEM_ADMIN، تحقق من وجود سياق عيادة
        Long contextClinicId = getClinicContextFromRequest();

        if (contextClinicId == null) {
            throw new ForbiddenOperationException(
                    "SYSTEM_ADMIN يجب أن يحدد سياق العيادة للقيام بعمليات الكتابة. " +
                            "استخدم header 'X-Acting-Clinic-Id' لتحديد العيادة المستهدفة"
            );
        }

        // التحقق من وجود العيادة
        validateClinicExists(contextClinicId);

        // تسجيل الإجراء
        logSystemAdminAction(userPrincipal.getId(), contextClinicId, "CONTEXT_SWITCH");

        return contextClinicId;
    }

    /**
     * الحصول على معرف العيادة للقراءة فقط (لا يتطلب سياق لـ SYSTEM_ADMIN)
     */
    public Long getEffectiveClinicIdForRead(UserPrincipal userPrincipal, Long requestedClinicId) {
        // إذا لم يكن SYSTEM_ADMIN، تحقق من أن العيادة المطلوبة هي نفس عيادة المستخدم
        if (!UserRole.SYSTEM_ADMIN.name().equals(userPrincipal.getRole())) {
            if (requestedClinicId != null && !requestedClinicId.equals(userPrincipal.getClinicId())) {
                throw new ForbiddenOperationException("ليس لديك صلاحية للوصول إلى بيانات هذه العيادة");
            }
            return userPrincipal.getClinicId();
        }

        // SYSTEM_ADMIN يمكنه قراءة أي عيادة
        if (requestedClinicId != null) {
            validateClinicExists(requestedClinicId);
            return requestedClinicId;
        }

        // إذا لم يحدد عيادة، يمكن إرجاع null للحصول على كل العيادات
        return null;
    }

    /**
     * التحقق من قدرة المستخدم على إنشاء موارد في العيادة
     */
    public boolean canCreateInClinic(UserPrincipal userPrincipal, Long clinicId) {
        // SYSTEM_ADMIN يحتاج سياق صريح
        if (UserRole.SYSTEM_ADMIN.name().equals(userPrincipal.getRole())) {
            Long contextClinicId = getClinicContextFromRequest();
            return contextClinicId != null && contextClinicId.equals(clinicId);
        }

        // باقي المستخدمين يمكنهم الإنشاء في عيادتهم فقط
        return userPrincipal.getClinicId() != null &&
                userPrincipal.getClinicId().equals(clinicId);
    }

    /**
     * استخراج معرف العيادة من headers الطلب
     */
    private Long getClinicContextFromRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();

        // التحقق من header العيادة
        String clinicIdHeader = request.getHeader("X-Acting-Clinic-Id");
        if (clinicIdHeader != null && !clinicIdHeader.isEmpty()) {
            try {
                Long clinicId = Long.parseLong(clinicIdHeader);

                // التحقق من السبب
                String reason = request.getHeader("X-Acting-Reason");
                if (reason == null || reason.isEmpty()) {
                    throw new BadRequestException(
                            "يجب توفير سبب لتبديل السياق في header 'X-Acting-Reason'"
                    );
                }

                logger.info("SYSTEM_ADMIN acting as clinic {} - Reason: {}", clinicId, reason);
                return clinicId;

            } catch (NumberFormatException e) {
                throw new BadRequestException("معرف العيادة غير صحيح في X-Acting-Clinic-Id");
            }
        }

        return null;
    }

    /**
     * التحقق من وجود العيادة
     */
    @Cacheable("clinics")
    private void validateClinicExists(Long clinicId) {
        if (!clinicRepository.existsById(clinicId)) {
            throw new ResourceNotFoundException("العيادة المحددة غير موجودة: " + clinicId);
        }
    }

    /**
     * تسجيل إجراءات SYSTEM_ADMIN
     */
    private void logSystemAdminAction(Long adminUserId, Long targetClinicId, String actionType) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String reason = request.getHeader("X-Acting-Reason");
                String requestPath = request.getRequestURI();
                String method = request.getMethod();

                SystemAdminAction action = new SystemAdminAction();
                action.setAdminUserId(adminUserId);
                action.setActionType(actionType);
                action.setTargetClinicId(targetClinicId);
                action.setReason(reason);
                action.setRequestPath(requestPath);
                action.setRequestMethod(method);
                action.setCreatedAt(LocalDateTime.now());

                actionRepository.save(action);

                logger.info("System Admin Action Logged: User {} performed {} on clinic {} - {}",
                        adminUserId, actionType, targetClinicId, reason);
            }
        } catch (Exception e) {
            logger.error("Failed to log system admin action", e);
            // Don't fail the main operation if logging fails
        }
    }

    /**
     * الحصول على معلومات السياق الحالي
     */
    public ClinicContextInfo getCurrentContext(UserPrincipal userPrincipal) {
        ClinicContextInfo info = new ClinicContextInfo();
        info.setUserId(userPrincipal.getId());
        info.setUserRole(userPrincipal.getRole());
        info.setOriginalClinicId(userPrincipal.getClinicId());

        if (UserRole.SYSTEM_ADMIN.name().equals(userPrincipal.getRole())) {
            Long contextClinicId = getClinicContextFromRequest();
            info.setActingAsClinicId(contextClinicId);

            if (contextClinicId != null) {
                Optional<Clinic> clinic = clinicRepository.findById(contextClinicId);
                clinic.ifPresent(c -> info.setActingAsClinicName(c.getName()));

                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    info.setReason(attributes.getRequest().getHeader("X-Acting-Reason"));
                }
            }
        }

        return info;
    }

    /**
     * DTO لمعلومات السياق
     */
    public static class ClinicContextInfo {
        private Long userId;
        private String userRole;
        private Long originalClinicId;
        private Long actingAsClinicId;
        private String actingAsClinicName;
        private String reason;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUserRole() { return userRole; }
        public void setUserRole(String userRole) { this.userRole = userRole; }

        public Long getOriginalClinicId() { return originalClinicId; }
        public void setOriginalClinicId(Long originalClinicId) {
            this.originalClinicId = originalClinicId;
        }

        public Long getActingAsClinicId() { return actingAsClinicId; }
        public void setActingAsClinicId(Long actingAsClinicId) {
            this.actingAsClinicId = actingAsClinicId;
        }

        public String getActingAsClinicName() { return actingAsClinicName; }
        public void setActingAsClinicName(String actingAsClinicName) {
            this.actingAsClinicName = actingAsClinicName;
        }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public boolean isActingAsClinic() {
            return actingAsClinicId != null;
        }
    }
}