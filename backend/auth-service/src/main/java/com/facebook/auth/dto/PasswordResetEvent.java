package com.facebook.auth.dto;

public record PasswordResetEvent(
        String email,
        String username,
        String resetToken) {
}
