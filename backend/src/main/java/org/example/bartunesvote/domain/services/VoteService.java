package org.example.bartunesvote.domain.services;
import org.example.bartunesvote.domain.model.Song;
import org.example.bartunesvote.domain.model.VoteUI;

public interface VoteService {
    Song getWinner();

    void resetVotes();

    boolean add(VoteUI voteUI);
}
