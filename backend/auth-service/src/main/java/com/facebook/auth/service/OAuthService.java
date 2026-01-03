package com.facebook.auth.service;

import com.facebook.auth.dto.AuthResponse;
import com.facebook.auth.model.AuthProvider;
import com.facebook.auth.model.OAuthConnection;
import com.facebook.auth.model.Role;
import com.facebook.auth.model.User;
import com.facebook.auth.repository.OAuthConnectionRepository;
import com.facebook.auth.repository.UserRepository;
import com.facebook.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final UserRepository userRepository;
    private final OAuthConnectionRepository oauthConnectionRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${application.security.oauth2.google.client-id}")
    private String googleClientId;

    @Value("${application.security.oauth2.google.client-secret}")
    private String googleClientSecret;

    @Value("${application.security.oauth2.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${application.security.oauth2.facebook.client-id}")
    private String facebookClientId;

    @Value("${application.security.oauth2.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${application.security.oauth2.facebook.redirect-uri}")
    private String facebookRedirectUri;

    public String getAuthorizationUrl(String provider) {
        if ("google".equalsIgnoreCase(provider)) {
            return "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + googleClientId +
                    "&redirect_uri=" + googleRedirectUri +
                    "&response_type=code" +
                    "&scope=email%20profile" +
                    "&access_type=offline";
        } else if ("facebook".equalsIgnoreCase(provider)) {
            return "https://www.facebook.com/v18.0/dialog/oauth" +
                    "?client_id=" + facebookClientId +
                    "&redirect_uri=" + facebookRedirectUri +
                    "&scope=email,public_profile";
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }

    public AuthResponse processCallback(String provider, String code) {
        if ("google".equalsIgnoreCase(provider)) {
            return processGoogleCallback(code);
        } else if ("facebook".equalsIgnoreCase(provider)) {
            return processFacebookCallback(code);
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }

    private AuthResponse processGoogleCallback(String code) {
        // Exchange code for token
        String tokenUrl = "https://oauth2.googleapis.com/token";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("redirect_uri", googleRedirectUri);
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

        Map<String, Object> tokenBody = (Map<String, Object>) tokenResponse.getBody();
        String accessToken = (String) tokenBody.get("access_token");
        String refreshToken = (String) tokenBody.get("refresh_token");

        // Get User Info
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);

        ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, new HttpEntity<>(authHeaders), Map.class);

        Map<String, Object> userInfo = (Map<String, Object>) userInfoResponse.getBody();
        String email = (String) userInfo.get("email");
        String googleId = (String) userInfo.get("sub");
        String name = (String) userInfo.get("name");

        return processOAuthUser(AuthProvider.GOOGLE, googleId, email, name, accessToken, refreshToken);
    }

    private AuthResponse processFacebookCallback(String code) {
        // Exchange code for token
        String tokenUrl = "https://graph.facebook.com/v18.0/oauth/access_token" +
                "?client_id=" + facebookClientId +
                "&redirect_uri=" + facebookRedirectUri +
                "&client_secret=" + facebookClientSecret +
                "&code=" + code;

        ResponseEntity<Map> tokenResponse = restTemplate.getForEntity(tokenUrl, Map.class);
        Map<String, Object> tokenBody = (Map<String, Object>) tokenResponse.getBody();
        String accessToken = (String) tokenBody.get("access_token");

        // Get User Info
        String userInfoUrl = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
        ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

        Map<String, Object> userInfo = (Map<String, Object>) userInfoResponse.getBody();
        String email = (String) userInfo.get("email");
        String facebookId = (String) userInfo.get("id");
        String name = (String) userInfo.get("name");

        // Fallback for email if not provided (Facebook doesn't always provide it)
        if (email == null) {
            email = facebookId + "@facebook.com";
        }

        return processOAuthUser(AuthProvider.FACEBOOK, facebookId, email, name, accessToken, null);
    }

    private AuthResponse processOAuthUser(AuthProvider provider, String providerId, String email, String name,
            String accessToken, String refreshToken) {
        Optional<OAuthConnection> connectionOpt = oauthConnectionRepository.findByProviderAndProviderUserId(provider,
                providerId);

        User user;
        if (connectionOpt.isPresent()) {
            OAuthConnection connection = connectionOpt.get();
            user = connection.getUser();

            // Update connection details
            connection.setAccessToken(accessToken);
            if (refreshToken != null) {
                connection.setRefreshToken(refreshToken);
            }
            connection.setLastLoginAt(LocalDateTime.now());
            oauthConnectionRepository.save(connection);
        } else {
            // Check if user exists by email
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                user = userOpt.get();
            } else {
                // Create new user
                user = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Random password
                        .role(Role.USER)
                        .provider(provider)
                        .build();
                user = userRepository.save(user);
            }

            // Create connection
            OAuthConnection connection = OAuthConnection.builder()
                    .user(user)
                    .provider(provider)
                    .providerUserId(providerId)
                    .email(email)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .lastLoginAt(LocalDateTime.now())
                    .build();
            oauthConnectionRepository.save(connection);
        }

        // Generate JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        String jwtToken = jwtService.generateToken(claims, user);

        // Create Application Refresh Token
        var appRefreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(appRefreshToken.getToken())
                .build();
    }
}
