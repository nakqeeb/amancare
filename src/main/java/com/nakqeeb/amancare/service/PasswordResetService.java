// ===================================================================
// 5. PASSWORD RESET SERVICE
// ===================================================================
package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.entity.PasswordResetToken;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.repository.PasswordResetTokenRepository;
import com.nakqeeb.amancare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.token-validity-hours:24}")
    private int tokenValidityHours;

    /**
     * إنشاء طلب إعادة تعيين كلمة المرور وإرسال البريد الإلكتروني
     */
    @Transactional
    public void initiatePasswordReset(String email) {
        // البحث عن المستخدم
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // لأسباب أمنية، لا نخبر المستخدم أن البريد غير موجود
            log.warn("محاولة إعادة تعيين كلمة مرور لبريد غير موجود: {}", email);
            return;
        }

        User user = userOpt.get();

        // حذف أي رموز سابقة لهذا البريد
        tokenRepository.deleteByEmail(email);

        // إنشاء رمز جديد
        String token = generateSecureToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenValidityHours);

        PasswordResetToken resetToken = new PasswordResetToken(token, email, expiryDate);
        tokenRepository.save(resetToken);

        // إرسال البريد الإلكتروني
        try {
            emailService.sendPasswordResetEmail(email, token, user.getFullName());
            log.info("تم إرسال رابط إعادة تعيين كلمة المرور إلى: {}", email);
        } catch (Exception e) {
            log.error("فشل في إرسال بريد إعادة تعيين كلمة المرور: {}", e.getMessage());
            throw new RuntimeException("فشل في إرسال البريد الإلكتروني");
        }
    }

    /**
     * التحقق من صحة رمز إعادة التعيين
     */
    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        return tokenOpt.isPresent() && tokenOpt.get().isValid();
    }

    /**
     * إعادة تعيين كلمة المرور باستخدام الرمز
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("رمز إعادة التعيين غير صحيح");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (!resetToken.isValid()) {
            throw new RuntimeException("انتهت صلاحية رمز إعادة التعيين");
        }

        // البحث عن المستخدم وتحديث كلمة المرور
        Optional<User> userOpt = userRepository.findByEmail(resetToken.getEmail());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("المستخدم غير موجود");
        }

        User user = userOpt.get();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // تحديد الرمز كمستخدم
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // إرسال تأكيد
        emailService.sendPasswordResetConfirmation(user.getEmail(), user.getFullName());

        log.info("تم تغيير كلمة المرور للمستخدم: {}", user.getEmail());
    }

    /**
     * إنشاء رمز آمن
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64]; // 512 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * تنظيف الرموز المنتهية الصلاحية (يجب استدعاؤها دورياً)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("تم تنظيف الرموز المنتهية الصلاحية");
    }
}