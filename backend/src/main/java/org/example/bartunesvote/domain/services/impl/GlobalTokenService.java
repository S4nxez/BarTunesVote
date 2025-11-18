package org.example.bartunesvote.domain.services.impl;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class GlobalTokenService {

    private OAuth2TokenContext tokenContext;

    public synchronized void storeTokenContext(String registrationId,
                                               String principalName,
                                               OAuth2AccessToken accessToken,
                                               OAuth2RefreshToken refreshToken) {
        if (accessToken == null) {
            this.tokenContext = null;
            return;
        }

        this.tokenContext = new OAuth2TokenContext(
                registrationId,
                principalName,
                accessToken.getTokenValue(),
                refreshToken != null ? refreshToken.getTokenValue() : null,
                accessToken.getExpiresAt()
        );
    }

    public synchronized void updateTokens(String newAccessToken,
                                          String newRefreshToken,
                                          Instant newExpiresAt) {
        if (tokenContext == null) return;

        this.tokenContext = new OAuth2TokenContext(
                tokenContext.clientRegistrationId(),
                tokenContext.principalName(),
                newAccessToken,
                newRefreshToken != null ? newRefreshToken : tokenContext.refreshToken(),
                newExpiresAt
        );
    }

    public synchronized Optional<OAuth2TokenContext> getTokenContext() {
        return Optional.ofNullable(tokenContext);
    }

    public synchronized String getAccessToken() {
        return tokenContext != null ? tokenContext.accessToken() : null;
    }

    public synchronized boolean isTokenAvailable() {
        return tokenContext != null && tokenContext.accessToken() != null;
    }

    public record OAuth2TokenContext(String clientRegistrationId,
                                     String principalName,
                                     String accessToken,
                                     String refreshToken,
                                     Instant expiresAt) {

        public OAuth2TokenContext withNewAccessToken(String tokenValue, Instant expiresAt) {
            return new OAuth2TokenContext(clientRegistrationId, principalName, tokenValue, refreshToken, expiresAt);
        }

        public OAuth2TokenContext withNewRefreshToken(String refreshTokenValue) {
            return new OAuth2TokenContext(clientRegistrationId, principalName, accessToken, refreshTokenValue, expiresAt);
        }
    }
}

