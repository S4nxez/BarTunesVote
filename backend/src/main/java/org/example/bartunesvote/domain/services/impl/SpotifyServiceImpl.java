package org.example.bartunesvote.domain.services.impl;

import org.example.bartunesvote.Constantes;
import org.example.bartunesvote.domain.model.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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

    private List<Song> canciones;

    private final OAuth2TokenService tokenService;

    public SpotifyServiceImpl(OAuth2TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;


    public void playSong( String trackId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenService.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Define el cuerpo de la solicitud con el URI de la canción
        Map<String, Object> body = new HashMap<>();
        body.put("uris", Collections.singletonList("spotify:track:" + trackId));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // Realiza la solicitud PUT a la API de Spotify
        restTemplate.exchange(SPOTIFY_PLAY_URL, HttpMethod.PUT, entity, Void.class);
    }

    public int getTrackDurationInSeconds(String trackId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenService.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Realiza la solicitud GET a la API de Spotify para obtener información del track
        String url = "https://api.spotify.com/v1/tracks/" + trackId; // Endpoint para obtener detalles del track
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Realiza la solicitud GET
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        // Extrae la duración de la respuesta
        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> trackData = response.getBody();
            if (trackData != null && trackData.containsKey("duration_ms")) {
                int durationMs = (int) trackData.get("duration_ms");
                return durationMs / 1000; // Convertir de milisegundos a segundos
            }
        }

        // En caso de fallo o si no se encuentra la duración, lanza una excepción o devuelve un valor por defecto
        throw new IllegalStateException("No se pudo obtener la duración del track.");
    }


    public List<Song> getCanciones(){
        return canciones;
    }

    public void setFourSongsFromPlaylist(String playlistId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenService.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://api.spotify.com/v1/playlists/" +  playlistId + "/tracks"; // Añadí "/tracks" al final
        Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        List<Song> songs = new ArrayList<>();
        String[] places = {"A", "B", "C", "D"};
        int index = 0;
        Random random = new Random();

        Set<String> addedSongNames = new HashSet<>();
        for (; index < 4; ) {
            Map<String, Object> track = (Map<String, Object>) items
                    .get(random.nextInt(items.size())).get("track");
            String songName = (String) track.get("name");
            String trackId = (String) track.get("id");

            if (!addedSongNames.contains(songName)) {
                songs.add(new Song(songName, trackId, places[index]));
                addedSongNames.add(songName);
                index++;
            }
        }

        this.canciones = songs;
    }
}

