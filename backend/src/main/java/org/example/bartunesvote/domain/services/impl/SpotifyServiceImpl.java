package org.example.bartunesvote.domain.services.impl;

import org.example.bartunesvote.domain.model.Song;
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

import java.math.BigDecimal;
import java.util.*;

@Service
public class SpotifyServiceImpl {
    private static final String SPOTIFY_PLAY_URL = "https://api.spotify.com/v1/me/player/play";

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

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

    public List<Song> getFourSongsFromPlaylist(String accessToken, String playlistId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks"; // Añadí "/tracks" al final
        Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        List<Song> songs = new ArrayList<>();
        String[] places = {"A", "B", "C", "D"};
        int index = 0;

        for (; index<4;index++ ) {
            Map<String, Object> track = (Map<String, Object>) items.get(index).get("track");
            String songName = (String) track.get("name");
            String trackId = (String) track.get("id");
            songs.add(new Song(songName, trackId, places[index]));
        }

        return songs;
    }
}

