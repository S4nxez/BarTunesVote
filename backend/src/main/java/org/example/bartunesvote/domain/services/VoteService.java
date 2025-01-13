package org.example.bartunesvote.domain.services;
import org.example.bartunesvote.domain.model.SongCard;
import org.example.bartunesvote.domain.model.VoteUI;

public interface VoteService {
    SongCard getWinner();

    void resetVotes();

    boolean add(VoteUI voteUI);
}
