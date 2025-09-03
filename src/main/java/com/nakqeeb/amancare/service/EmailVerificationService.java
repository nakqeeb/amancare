// ===================================================================
// 4. EMAIL VERIFICATION SERVICE
// src/main/java/com/nakqeeb/amancare/service/EmailVerificationService.java
// ===================================================================
package com.nakqeeb.amancare.service;

import com.nakqeeb.amancare.entity.EmailVerificationToken;
import com.nakqeeb.amancare.entity.User;
import com.nakqeeb.amancare.repository.EmailVerificationTokenRepository;
import com.nakqeeb.amancare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.email-verification.token-validity-hours:24}")
    private int tokenValidityHours;

    /**
     * إنشاء وإرسال رمز تأكيد البريد الإلكتروني
     */
    @Transactional
    public void sendVerificationEmail(User user) {
        try {
            // حذف أي رموز سابقة للمستخدم
            tokenRepository.deleteByUserId(user.getId());

            // إنشاء رمز جديد
            String token = generateSecureToken();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenValidityHours);

            EmailVerificationToken verificationToken = new EmailVerificationToken(
                    token,
                    user.getEmail(),
                    user.getId(),
                    expiryDate
            );
            tokenRepository.save(verificationToken);

            // إرسال البريد الإلكتروني
            emailService.sendEmailVerificationEmail(
                    user.getEmail(),
                    token,
                    user.getFullName(),
                    user.getClinic().getName()
            );

            log.info("تم إرسال بريد تأكيد البريد الإلكتروني إلى: {}", user.getEmail());
        } catch (Exception e) {
            log.error("فشل في إرسال بريد تأكيد البريد الإلكتروني: {}", e.getMessage());
            throw new RuntimeException("فشل في إرسال بريد التأكيد");
        }
    }

    /**
     * التحقق من صحة رمز التأكيد
     */
    public boolean validateVerificationToken(String token) {
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByToken(token);
        return tokenOpt.isPresent() && tokenOpt.get().isValid();
    }

    /**
     * تفعيل الحساب باستخدام رمز التأكيد
     */
    @Transactional
    public void verifyEmail(String token) {
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("رمز التأكيد غير صحيح");
        }

        EmailVerificationToken verificationToken = tokenOpt.get();

        if (!verificationToken.isValid()) {
            throw new RuntimeException("انتهت صلاحية رمز التأكيد");
        }

        // البحث عن المستخدم وتفعيل الحساب
        Optional<User> userOpt = userRepository.findById(verificationToken.getUserId());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("المستخدم غير موجود");
        }

        User user = userOpt.get();
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // تحديد الرمز كمستخدم
        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        // إرسال تأكيد التفعيل
        emailService.sendAccountActivatedEmail(user.getEmail(), user.getFullName());

        log.info("تم تفعيل حساب المستخدم: {}", user.getEmail());
    }

    /**
     * إعادة إرسال رابط التأكيد
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.warn("محاولة إعادة إرسال تأكيد لبريد غير موجود: {}", email);
            return; // لأسباب أمنية، لا نخبر المستخدم أن البريد غير موجود
        }

        User user = userOpt.get();

        if (user.getIsActive()) {
            throw new RuntimeException("الحساب مفعل بالفعل");
        }

        // حذف الرموز السابقة وإرسال رمز جديد
        sendVerificationEmail(user);

        log.info("تم إعادة إرسال رابط التأكيد إلى: {}", email);
    }

    /**
     * إنشاء رمز آمن
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * تنظيف الرموز المنتهية الصلاحية
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("تم تنظيف رموز تأكيد البريد الإلكتروني المنتهية الصلاحية");
    }
}
