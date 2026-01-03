package com.facebook.auth.repository;

import com.facebook.auth.model.AuthProvider;
import com.facebook.auth.model.OAuthConnection;
import com.facebook.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuthConnectionRepository extends JpaRepository<OAuthConnection, Long> {
    Optional<OAuthConnection> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<OAuthConnection> findByUserAndProvider(User user, AuthProvider provider);

    List<OAuthConnection> findByUser(User user);

    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
