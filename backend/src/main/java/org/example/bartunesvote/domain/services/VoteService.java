package org.example.bartunesvote.domain.services;
import org.example.bartunesvote.domain.model.Song;
import org.example.bartunesvote.domain.model.VoteUI;

public interface VoteService {
    Song getWinner();

    void resetVotes();

    void add(VoteUI voteUI);
}
