package org.example.bartunesvote.domain.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.bartunesvote.domain.services.impl.GlobalTokenService.OAuth2TokenContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Log4j2
public class OAuth2TokenService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GlobalTokenService globalTokenService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://accounts.spotify.com/api")
            .build();

    public void storeTokensFromAuthentication(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            log.warn("No se pudo cargar el cliente OAuth2 para guardar tokens");
            return;
        }

        globalTokenService.storeTokenContext(
                client.getClientRegistration().getRegistrationId(),
                authentication.getName(),
                client.getAccessToken(),
                client.getRefreshToken()
        );
    }

    public String getAccessToken() {
        Optional<OAuth2TokenContext> contextOpt = ensureTokenContext();

        if (contextOpt.isEmpty()) {
            if (SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken)
                log.warn("No hay contexto de token almacenado");
            else
                log.debug("No hay autenticación OAuth2 en curso, no se estableció token");
            return null;
        }

        OAuth2TokenContext context = contextOpt.get();
        if (context.expiresAt() == null || context.expiresAt().isBefore(Instant.now().plusSeconds(60))) {
            log.debug("Token expirado o cercano a expirar, intentando refrescar");
            refreshAccessToken(context);
        }

        return globalTokenService.getAccessToken();
    }

    public boolean isTokenAvailable() {
        return globalTokenService.isTokenAvailable();
    }

    private void refreshAccessToken(OAuth2TokenContext context) {
        if (context.refreshToken() == null) {
            log.warn("No hay refresh token disponible para {}", context.principalName());
            return;
        }

        ClientRegistration registration = getClientRegistration(context.clientRegistrationId());
        if (registration == null) {
            log.error("No se encontró ClientRegistration para id {}", context.clientRegistrationId());
            return;
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", context.refreshToken());

        String basicAuth = buildBasicAuthHeader(
                registration.getClientId(),
                registration.getClientSecret()
        );

        try {
            SpotifyTokenResponse response = webClient.post()
                    .uri("/token")
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .bodyToMono(SpotifyTokenResponse.class)
                    .blockOptional()
                    .orElse(null);

            if (response == null || response.getAccessToken() == null) {
                log.error("No se recibió un token válido al refrescar");
                return;
            }

            Instant expiresAt = Instant.now().plus(response.getExpiresIn(), ChronoUnit.SECONDS);
            globalTokenService.updateTokens(response.getAccessToken(), response.getRefreshToken(), expiresAt);
        } catch (Exception e) {
            log.error("Error al refrescar el token", e);
        }
    }

    private ClientRegistration getClientRegistration(String registrationId) {
        if (registrationId == null) {
            return null;
        }
        return clientRegistrationRepository.findByRegistrationId(registrationId);
    }

    private Optional<OAuth2TokenContext> ensureTokenContext() {
        Optional<OAuth2TokenContext> context = globalTokenService.getTokenContext();
        if (context.isPresent()) {
            return context;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauth2Token.getAuthorizedClientRegistrationId(),
                    oauth2Token.getName()
            );

            if (client != null && client.getAccessToken() != null) {
                globalTokenService.storeTokenContext(
                        client.getClientRegistration().getRegistrationId(),
                        oauth2Token.getName(),
                        client.getAccessToken(),
                        client.getRefreshToken()
                );
                return globalTokenService.getTokenContext();
            }
        }

        return Optional.empty();
    }

    private String buildBasicAuthHeader(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}

