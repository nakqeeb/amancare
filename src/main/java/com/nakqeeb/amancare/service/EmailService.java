// ===================================================================
// 4. EMAIL SERVICE
// ===================================================================
package com.nakqeeb.amancare.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * إرسال بريد إلكتروني بسيط
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("تم إرسال البريد الإلكتروني البسيط إلى: {}", to);
        } catch (Exception e) {
            log.error("خطأ في إرسال البريد الإلكتروني إلى {}: {}", to, e.getMessage());
            throw new RuntimeException("فشل في إرسال البريد الإلكتروني");
        }
    }

    /**
     * إرسال بريد إلكتروني بقالب HTML
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            // إنشاء محتوى HTML من القالب
            Context context = new Context();
            variables.forEach(context::setVariable);
            String htmlContent = templateEngine.process(templateName, context);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("تم إرسال البريد الإلكتروني HTML إلى: {}", to);
        } catch (MessagingException e) {
            log.error("خطأ في إرسال البريد الإلكتروني HTML إلى {}: {}", to, e.getMessage());
            throw new RuntimeException("فشل في إرسال البريد الإلكتروني");
        }
    }

    /**
     * إرسال رابط إعادة تعيين كلمة المرور
     */
    public void sendPasswordResetEmail(String email, String token, String userName) {
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;

        Map<String, Object> variables = Map.of(
                "userName", userName,
                "resetUrl", resetUrl,
                "expiryTime", "24 ساعة"
        );

        sendHtmlEmail(
                email,
                "إعادة تعيين كلمة المرور - نظام أمان كير",
                "password-reset-email",
                variables
        );
    }

    /**
     * إرسال تأكيد إعادة تعيين كلمة المرور
     */
    public void sendPasswordResetConfirmation(String email, String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName
        );

        sendHtmlEmail(
                email,
                "تم تغيير كلمة المرور بنجاح - نظام أمان كير",
                "password-reset-success",
                variables
        );
    }
}