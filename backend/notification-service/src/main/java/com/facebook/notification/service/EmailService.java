package com.facebook.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.from:no-reply@localhost}")
    private String fromEmail;

    @Async
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        log.info("Sending password reset email to: {}", to);
        try {
            Context context = new Context();
            context.setVariables(Map.of(
                    "username", username,
                    "resetLink", "http://localhost:5173/reset-password?token=" + resetToken));

            String html = templateEngine.process("password-reset", context);
            sendHtmlEmail(to, "Reset Your Password - Facebook Clone", html);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}", to, e);
        }
    }

    @Async
    public void sendOfflineNotification(String to, String username, String senderName, String messageContent) {
        // Only send if we have a valid email (skipped for now as we might only have
        // UUIDs in some contexts)
        // Ideally User Service would provide the email, or we'd store it in Redis
        if (to == null || !to.contains("@")) {
            log.warn("Cannot send offline notification: Invalid email address {}", to);
            return;
        }

        log.info("Sending offline notification to: {}", to);
        try {
            Context context = new Context();
            context.setVariables(Map.of(
                    "username", username,
                    "senderName", senderName,
                    "messageContent", messageContent,
                    "chatLink", "http://localhost:5173/messages"));

            String html = templateEngine.process("offline-notification", context);
            sendHtmlEmail(to, "New Message from " + senderName, html);
        } catch (MessagingException e) {
            log.error("Failed to send offline notification to {}", to, e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
        log.info("Email sent successfully to {}", to);
    }
}
