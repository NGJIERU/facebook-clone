package com.facebook.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${application.mail.from-email}")
    private String fromEmail;

    @Value("${application.mail.enabled}")
    private boolean mailEnabled;

    public void sendSimpleMessage(String to, String subject, String text) {
        log.info("Preparing to send email to: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body: {}", text);

        if (mailEnabled) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                emailSender.send(message);
                log.info("Email sent successfully.");
            } catch (Exception e) {
                log.error("Failed to send email", e);
                // Don't rethrow, just log, so flow doesn't break if mail server is down in dev
            }
        } else {
            log.info("Mail sending is disabled in configuration. Skipping actual send.");
        }
    }
}
