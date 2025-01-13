package org.example.bartunesvote.domain.services.impl;

import lombok.Setter;
import org.example.bartunesvote.domain.model.SongCard;
import org.example.bartunesvote.domain.model.VoteUI;
import org.example.bartunesvote.domain.services.VoteService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Setter
public class VoteServiceImpl implements VoteService {
    private final List<SongCard> canciones = new ArrayList<>();
    private List<String> sesiones = new ArrayList<>();

    public VoteServiceImpl() {
        // Inicializar los contadores de votos para A, B, C y D
        canciones.add(new SongCard("A", BigDecimal.ZERO));
        canciones.add(new SongCard("B", BigDecimal.ZERO));
        canciones.add(new SongCard("C", BigDecimal.ZERO));
        canciones.add(new SongCard("D", BigDecimal.ZERO));
    }

    @Override
    public SongCard getWinner() {
        // Encontrar la opción con más votos
        return canciones.stream()
                .max(Comparator.comparing(SongCard::getVotes))
                .orElse(null);
    }

    @Override
    public void resetVotes() {
        // Reiniciar todos los contadores
        canciones.forEach(song -> song.setVotes(BigDecimal.ZERO));
        sesiones.clear();
    }

    @Override
    public boolean add(VoteUI voteUI) {
        // Sumar un voto a la opción seleccionada
        Optional<String> sessionOpt = sesiones.stream().filter(session ->
                                session.equals(voteUI.getSessionId())).findFirst();

        if (sessionOpt.isPresent())
            return false;
        sesiones.add(voteUI.getSessionId());

        canciones.stream().filter(cancion ->
                cancion.getPlace().equals(voteUI.getPlace())
        ).forEach(SongCard::addVote);
        return true;
    }
}
