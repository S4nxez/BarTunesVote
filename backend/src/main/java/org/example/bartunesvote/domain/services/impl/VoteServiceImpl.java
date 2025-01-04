package org.example.bartunesvote.domain.services.impl;

import org.example.bartunesvote.domain.model.Song;
import org.example.bartunesvote.domain.model.VoteUI;
import org.example.bartunesvote.domain.services.VoteService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class VoteServiceImpl implements VoteService {
    private final List<Song> canciones = new ArrayList<>();

    public VoteServiceImpl() {
        // Inicializar los contadores de votos para A, B, C y D
        canciones.add(new Song("Bohemian Rhapsody","Bohemian Rhapsody", "A", BigDecimal.ZERO));
        canciones.add(new Song("Shape of You","Shape of You","B", BigDecimal.ZERO));
        canciones.add(new Song("Sweet Child O Mine", "Sweet Child O Mine", "C", BigDecimal.ZERO));
        canciones.add(new Song("Lose Yourself", "Lose Yourself", "D", BigDecimal.ZERO));
    }

    @Override
    public Song getWinner() {
        // Encontrar la opción con más votos
        return canciones.stream()
                .max(Comparator.comparing(Song::getVotes))
                .orElse(null);
    }

    @Override
    public void resetVotes() {
        // Reiniciar todos los contadores
        canciones.forEach(song -> song.setVotes(BigDecimal.ZERO));
    }

    @Override
    public void add(VoteUI voteUI) {
        // Sumar un voto a la opción seleccionada
        canciones.stream().filter(cancion ->
                cancion.getSongId().equals(voteUI.getSongId())
        ).forEach(Song::addVote);
    }
}
