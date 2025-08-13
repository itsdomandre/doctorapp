package com.domandre.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${mail.confirmation.sender}")
    private String sender;

    @Value("${app.backend.url}")
    private String backendUrl;

    @Value("${app.frontend.url:}")
    private String frontendUrl;

    private String baseUrl() {
        return (frontendUrl != null && !frontendUrl.isBlank()) ? frontendUrl : backendUrl;
    }

    public void sendActivationEmail(String to, String token) {
        String link;
        if (frontendUrl == null || frontendUrl.isBlank()) {
            link = backendUrl + "/api/auth/activate?token=" + token;
        } else {
            link = frontendUrl + "/activate?token=" + token;
        }

        sendEmail(
                to,
                "Activate your account",
                "<p>Click the link below to activate your account:</p>" +
                        "<p><a href=\"" + link + "\">Activate Now</a></p>" +
                        "<p>Or use this token directly: <code>" + token + "</code></p>"
        );
    }

    public void sendResetEmail(String to, String token) {
        String link;
        if (frontendUrl == null || frontendUrl.isBlank()) {
            link = backendUrl + "/api/auth/reset-password?token=" + token;
        } else {
            link = frontendUrl + "/reset-password?token=" + token;
        }

        sendEmail(
                to,
                "Password reset request",
                "<p>Click the link below to reset your password:</p>" +
                        "<p><a href=\"" + link + "\">Reset Password</a></p>" +
                        "<p>Or use this token directly: <code>" + token + "</code></p>"
        );
    }

    public void sendWelcomeEmail(String to, String firstName) {
        sendEmail(
                to,
                "Welcome to DoctorApp 🎉",
                "<h3>Hi " + firstName + ",</h3>" +
                        "<p>Welcome aboard! We’re happy to have you with us.</p>" +
                        "<p>You can now log in and start using our services:</p>" +
                        "<p><a href=\"" + baseUrl() + "/login\">Go to Login</a></p>" +
                        "<br>" +
                        "<p>— The DoctorApp Team</p>"
        );
    }

    public void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }


}
