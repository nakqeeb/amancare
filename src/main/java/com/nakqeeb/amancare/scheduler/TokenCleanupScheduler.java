// ===================================================================
// 9. SCHEDULED TASK FOR TOKEN CLEANUP
// ===================================================================
package com.nakqeeb.amancare.scheduler;

import com.nakqeeb.amancare.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final PasswordResetService passwordResetService;

    /**
     * تنظيف الرموز المنتهية الصلاحية كل ساعة
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupExpiredTokens() {
        try {
            passwordResetService.cleanupExpiredTokens();
            log.debug("تم تنظيف رموز إعادة تعيين كلمة المرور المنتهية الصلاحية");
        } catch (Exception e) {
            log.error("خطأ في تنظيف الرموز المنتهية الصلاحية: {}", e.getMessage());
        }
    }
}