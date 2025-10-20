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

import java.time.LocalDate;
import java.time.LocalTime;
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

    /**
     * إرسال بريد تأكيد البريد الإلكتروني
     */
    public void sendEmailVerificationEmail(String email, String token, String userName, String clinicName) {
        String verificationUrl = frontendUrl + "/auth/verify-email?token=" + token;

        Map<String, Object> variables = Map.of(
                "userName", userName,
                "clinicName", clinicName,
                "verificationUrl", verificationUrl,
                "expiryTime", "24 ساعة"
        );

        sendHtmlEmail(
                email,
                "تأكيد البريد الإلكتروني - نظام أمان كير",
                "email-verification",
                variables
        );
    }

    /**
     * إرسال تأكيد تفعيل الحساب
     */
    public void sendAccountActivatedEmail(String email, String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName
        );

        sendHtmlEmail(
                email,
                "تم تفعيل حسابك بنجاح - نظام أمان كير",
                "account-activated",
                variables
        );
    }

    /**
     * إرسال بريد إعادة إرسال رابط التفعيل
     */
    public void sendResendVerificationEmail(String email, String token, String userName) {
        String verificationUrl = frontendUrl + "/auth/verify-email?token=" + token;

        Map<String, Object> variables = Map.of(
                "userName", userName,
                "verificationUrl", verificationUrl,
                "expiryTime", "24 ساعة"
        );

        sendHtmlEmail(
                email,
                "إعادة إرسال رابط تأكيد البريد الإلكتروني - نظام أمان كير",
                "resend-email-verification",
                variables
        );
    }

    /**
     * Send appointment confirmation email for guest booking
     */
    public void sendAppointmentConfirmationEmail(String email, String token, String patientName,
                                                 String patientNumber, String doctorName,
                                                 String clinicName, LocalDate appointmentDate,
                                                 LocalTime appointmentTime, Integer tokenNumber) {
        String confirmationUrl = frontendUrl + "/guest/confirm-appointment?token=" + token;
        String manageUrl = frontendUrl + "/guest/appointments?patientNumber=" + patientNumber;

        Map<String, Object> variables = Map.of(
                "patientName", patientName,
                "patientNumber", patientNumber,
                "doctorName", doctorName,
                "clinicName", clinicName,
                "appointmentDate", appointmentDate.toString(),
                "appointmentTime", appointmentTime.toString(),
                "tokenNumber", tokenNumber.toString(),
                "confirmationUrl", confirmationUrl,
                "manageUrl", manageUrl,
                "expiryTime", "48 ساعة"
        );

        sendHtmlEmail(
                email,
                "تأكيد موعدك - " + clinicName,
                "appointment-confirmation",
                variables
        );
    }
}