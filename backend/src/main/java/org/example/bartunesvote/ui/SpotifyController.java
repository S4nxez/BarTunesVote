package org.example.bartunesvote.ui;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.bartunesvote.domain.model.Song;
import org.example.bartunesvote.domain.services.impl.OAuth2TokenService;
import org.example.bartunesvote.domain.services.impl.SpotifyServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.example.bartunesvote.common.Constantes;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Controller
public class SpotifyController {
    private final SpotifyServiceImpl spotifyService;
    private final OAuth2TokenService tokenService;
    private final DynamicScheduler dynamicScheduler;

    @GetMapping("/login")
    public String login() {
        return "redirect:" + Constantes.AUTHORIZATION_URL;
    }

    @GetMapping("/post-login")
    public String handlePostLogin() {
        OAuth2AuthenticationToken authentication =
                (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null)
            throw new IllegalStateException("El token de autenticación es nulo.");
        tokenService.storeTokensFromAuthentication(authentication);
        return "redirect:" + Constantes.DASHBOARD_URL;
    }

    @GetMapping("/songs")
    public ResponseEntity<List<Song>> getSongList() {
        try {
            return  (!tokenService.isTokenAvailable())
                    ? ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of())
                    : ResponseEntity.ok(spotifyService.getCanciones());
        } catch (Exception e) {
            log.error("Error al cargar playlist ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @GetMapping("/play/{playlistId}")
    public ResponseEntity<String> playSong(@PathVariable String playlistId) {
        try {
            spotifyService.setFourSongsFromPlaylist(playlistId);
            dynamicScheduler.start(playlistId);
            return ResponseEntity.ok("La canción se está reproduciendo en tu cuenta de Spotify.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al intentar reproducir la canción: " + e.getMessage());
        }
    }
}
