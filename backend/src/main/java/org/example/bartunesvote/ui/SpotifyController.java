package org.example.bartunesvote.ui;

import org.example.bartunesvote.domain.model.Song;
import org.example.bartunesvote.domain.services.impl.SpotifyServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SpotifyController {

    private SpotifyServiceImpl spotifyService;

    public SpotifyController(SpotifyServiceImpl spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/oauth2/authorization/spotify";
    }

    @GetMapping("/songs/{playlistId}")
    public ResponseEntity<List<Song>> getSongList(@PathVariable String playlistId, OAuth2AuthenticationToken authentication) {
        try {
            String accessToken = spotifyService.getAccessToken(authentication);
            System.out.println("Access Token: " + accessToken);
            List<Song> songs = spotifyService.getFourSongsFromPlaylist(accessToken, playlistId);
            System.out.println("Songs: " + songs);
            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            System.err.println("Error al cargar playlist: " + e.getMessage());
            throw new RuntimeException("Error al cargar playlist: " + e.getMessage());
        }
    }

    @GetMapping("/play")
    public ResponseEntity<String> playSong(OAuth2AuthenticationToken authentication) {
        try {
            // Obtén el token de acceso del usuario autenticado
            String accessToken = spotifyService.getAccessToken(authentication);

            // ID de la canción a reproducir
            String trackId = "7AraTawVyOl4TpLdDS5FP3";

            // Llama al servicio para reproducir la canción
            spotifyService.playSong(accessToken, trackId);

            return ResponseEntity.ok("La canción se está reproduciendo en tu cuenta de Spotify.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al intentar reproducir la canción: " + e.getMessage());
        }
    }
}
