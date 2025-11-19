package org.example.bartunesvote.domain.services;

import org.example.bartunesvote.domain.model.Song;

import java.util.List;

public interface SpotifyService {
    void playSong(String trackId);

    int getTrackDurationInSeconds(String trackId);

    void setFourSongsFromPlaylist(String playlistId);

    List<Song> getCanciones();
}
