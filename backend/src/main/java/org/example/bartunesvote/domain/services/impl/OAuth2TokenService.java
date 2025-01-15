package org.example.bartunesvote.domain.services.impl;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


@Service
public class OAuth2TokenService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GlobalTokenService globalTokenService;

    public OAuth2TokenService(OAuth2AuthorizedClientService authorizedClientService,
                              GlobalTokenService globalTokenService) {
        this.authorizedClientService = authorizedClientService;
        this.globalTokenService = globalTokenService;
    }

    public void storeAccessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );
        if (client != null && client.getAccessToken() != null) {
            globalTokenService.storeAccessToken(client.getAccessToken().getTokenValue());
        }
    }

    public String getAccessToken() {
        if (isTokenExpired()) {
            refreshAccessToken(); // Refresca el token si ha expirado
        }
        return globalTokenService.getAccessToken();
    }

    public boolean isTokenAvailable() {
        return globalTokenService.isTokenAvailable();
    }

    private boolean isTokenExpired() {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                "spotify", // Cambiar al ID de cliente registrado para Spotify
                "current-user" // Cambiar para identificar al usuario actual
        );
        if (client != null && client.getAccessToken() != null) {
            return client.getAccessToken().getExpiresAt().isBefore(Instant.now().plusSeconds(60));
        }
        return true;
    }

    private void refreshAccessToken() {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                "spotify",
                "current-user"
        );
        if (client != null) {
            // Spotify renueva el token usando el refresh token
            OAuth2RefreshToken refreshToken = client.getRefreshToken();
            if (refreshToken != null) {
                // Usar RestTemplate o WebClient para realizar la solicitud de renovación
                // Convertir el Map a MultiValueMap
                Map<String, String> body = new HashMap<>();
                body.put("grant_type", "refresh_token");
                body.put("refresh_token", refreshToken.getTokenValue());
                body.put("client_id", "<CLIENT_ID>");
                body.put("client_secret", "<CLIENT_SECRET>");

                MultiValueMap<String, String> multiValueBody = new LinkedMultiValueMap<>();
                body.forEach(multiValueBody::add);

                WebClient webClient = WebClient.create("https://accounts.spotify.com/api/token");

                String newAccessToken = webClient.post()
                        .body(BodyInserters.fromFormData(multiValueBody)) // Usar el MultiValueMap
                        .retrieve()
                        .bodyToMono(SpotifyTokenResponse.class) // Clase de respuesta
                        .block()
                        .getAccessToken();

                globalTokenService.storeAccessToken(newAccessToken);
            }
        }
    }
}

