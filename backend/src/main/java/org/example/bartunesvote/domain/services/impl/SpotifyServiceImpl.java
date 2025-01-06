package org.example.bartunesvote.domain.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class SpotifyServiceImpl {
    private static final String SPOTIFY_PLAY_URL = "https://api.spotify.com/v1/me/player/play";

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    public String getAccessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());
        return client.getAccessToken().getTokenValue();
    }

    public void playSong(String accessToken, String trackId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Define el cuerpo de la solicitud con el URI de la canción
        Map<String, Object> body = new HashMap<>();
        body.put("uris", Collections.singletonList("spotify:track:" + trackId));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // Realiza la solicitud PUT a la API de Spotify
        restTemplate.exchange(SPOTIFY_PLAY_URL, HttpMethod.PUT, entity, Void.class);
    }
}

