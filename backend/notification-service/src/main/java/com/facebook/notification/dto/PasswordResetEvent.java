package com.facebook.notification.dto;

public record PasswordResetEvent(
        String email,
        String username,
        String resetToken) {
}
