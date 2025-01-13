package org.example.bartunesvote.ui;

import org.example.bartunesvote.domain.model.Song;
import org.example.bartunesvote.domain.model.SongCard;
import org.example.bartunesvote.domain.services.impl.SpotifyServiceImpl;
import org.example.bartunesvote.domain.services.impl.VoteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class DynamicScheduler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final VoteServiceImpl voteServiceImpl;
    private int songDuration = 10; // Duración inicial en segundos

    private final SpotifyServiceImpl spotifyServiceImpl;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public DynamicScheduler(SpotifyServiceImpl spotifyServiceImpl, VoteServiceImpl voteServiceImpl) {
        this.spotifyServiceImpl = spotifyServiceImpl;
        this.voteServiceImpl = voteServiceImpl;
    }

    public void start() {
        scheduler.schedule(this::playSong, 0, TimeUnit.SECONDS); // Comienza inmediatamente
    }

    private void playSong() {
        SongCard winner = voteServiceImpl.getWinner();
        Song winnerSong = spotifyServiceImpl.getCanciones()
                .stream().filter(c->c.getPlace().equals(winner.getPlace()))
                .findFirst().get();
        this.songDuration = spotifyServiceImpl.getTrackDurationInSeconds(winnerSong.getSongId());
        spotifyServiceImpl.playSong(winnerSong.getSongId());
        spotifyServiceImpl.setFourSongsFromPlaylist();
        messagingTemplate.convertAndSend("/topic/updates", "Trigger GET request");
        // Programa la siguiente tarea al final de la canción
        scheduler.schedule(this::handleEndOfSong, songDuration, TimeUnit.SECONDS);
        voteServiceImpl.resetVotes();
    }

    private void handleEndOfSong() {
        System.out.println("Manejando fin de canción...");
        // Lógica para manejar fin de la canción
        playSong(); // Llama al siguiente ciclo
    }

    public void stop() {
        scheduler.shutdown();
    }
}
