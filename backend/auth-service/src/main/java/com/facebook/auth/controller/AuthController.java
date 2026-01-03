package com.facebook.auth.controller;

import com.facebook.auth.dto.AuthResponse;
import com.facebook.auth.dto.LoginRequest;
import com.facebook.auth.dto.RegisterRequest;
import com.facebook.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final com.facebook.auth.service.OAuthService oauthService;
    private final com.facebook.auth.service.PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @org.springframework.web.bind.annotation.GetMapping("/oauth2/authorize/{provider}")
    public void authorize(
            @org.springframework.web.bind.annotation.PathVariable String provider,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        String url = oauthService.getAuthorizationUrl(provider);
        response.sendRedirect(url);
    }

    @org.springframework.beans.factory.annotation.Value("${application.frontend-url}")
    private String frontendUrl;

    @org.springframework.web.bind.annotation.GetMapping("/oauth2/callback/{provider}")
    public void callback(
            @org.springframework.web.bind.annotation.PathVariable String provider,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String code,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String error,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {

        if (error != null) {
            response.sendRedirect(frontendUrl + "/login?error=" + error);
            return;
        }
        if (code == null) {
            response.sendRedirect(frontendUrl + "/login?error=no_code");
            return;
        }

        try {
            AuthResponse authResponse = oauthService.processCallback(provider, code);
            String redirectUrl = frontendUrl + "/login?token=" + authResponse.getToken() +
                    "&refreshToken=" + authResponse.getRefreshToken();
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/login?error="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody com.facebook.auth.dto.RefreshTokenRequest request) {
        return ResponseEntity.ok(service.refreshToken(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody com.facebook.auth.dto.ForgotPasswordRequest request) {
        passwordResetService.createPasswordResetTokenForUser(request.getEmail());
        return ResponseEntity.ok(java.util.Map.of("message", "If an account exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody com.facebook.auth.dto.ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(java.util.Map.of("message", "Password reset successfully."));
    }
}
