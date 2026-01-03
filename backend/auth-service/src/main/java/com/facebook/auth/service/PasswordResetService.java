package com.facebook.auth.service;

import com.facebook.auth.model.PasswordResetToken;
import com.facebook.auth.model.User;
import com.facebook.auth.repository.PasswordResetTokenRepository;
import com.facebook.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Delete existing tokens
            tokenRepository.deleteByUser(user);

            // Create new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken myToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusMinutes(30)) // 30 minutes expiry
                    .build();
            tokenRepository.save(myToken);

            // Send email
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String subject = "Reset Password";
            String text = "To reset your password, click the link below:\n" + resetUrl;

            emailService.sendSimpleMessage(user.getEmail(), subject, text);
        });
        // If user not found, we typically don't reveal it to prevent enumeration,
        // so we return normally or log internally.
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Expired password reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}
