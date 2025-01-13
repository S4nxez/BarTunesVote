package org.example.bartunesvote.ui;

import lombok.extern.log4j.Log4j2;
import org.example.bartunesvote.Constantes;
import org.example.bartunesvote.domain.model.Song;
import org.example.bartunesvote.domain.services.impl.OAuth2TokenService;
import org.example.bartunesvote.domain.services.impl.SpotifyServiceImpl;
import org.example.bartunesvote.domain.services.impl.VoteServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;


@Log4j2
@Controller
public class SpotifyController {
    private final SpotifyServiceImpl spotifyService;
    private final OAuth2TokenService tokenService;
    private final DynamicScheduler dynamicScheduler;
    private final VoteServiceImpl voteServiceImpl;

    public SpotifyController(SpotifyServiceImpl spotifyService,
                             OAuth2TokenService tokenService,
                             DynamicScheduler dynamicScheduler, VoteServiceImpl voteServiceImpl) {
        this.spotifyService = spotifyService;
        this.tokenService = tokenService;
        this.dynamicScheduler = dynamicScheduler;
        this.voteServiceImpl = voteServiceImpl;
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/oauth2/authorization/spotify";
    }

    @GetMapping("/post-login")
    public String handlePostLogin() {
        OAuth2AuthenticationToken authentication =
                (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("El token de autenticación es nulo.");
        }
        tokenService.storeAccessToken(authentication);
        spotifyService.setFourSongsFromPlaylist();
        return "redirect:/dashboard";
    }
    @GetMapping("/songs")
    public ResponseEntity<?> getSongList() {
        try {
            if (!tokenService.isTokenAvailable()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token not available");
            }

            String accessToken = tokenService.getAccessToken();
            log.info("Access Token: {}", accessToken);

            List<Song> songs = spotifyService.getCanciones();
            log.info("Songs: {}", songs);

            return ResponseEntity.ok(songs);
        } catch (Exception e) {
            log.error("Error al cargar playlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cargar playlist: " + e.getMessage());
        }
    }

    @GetMapping("/play")
    public ResponseEntity<String> playSong() {
        try {
            dynamicScheduler.start();
            return ResponseEntity.ok("La canción se está reproduciendo en tu cuenta de Spotify.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al intentar reproducir la canción: " + e.getMessage());
        }
    }
}
