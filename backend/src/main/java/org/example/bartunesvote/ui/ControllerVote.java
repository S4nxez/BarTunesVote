package org.example.bartunesvote.ui;

import org.example.bartunesvote.domain.model.Song;
import org.example.bartunesvote.domain.services.VoteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.example.bartunesvote.domain.model.VoteUI;


@RestController
@RequestMapping("/api/vote")
public class ControllerVote {
    private final VoteService voteService;

    public ControllerVote(VoteService voteService){
        this.voteService=voteService;
    }

    @PostMapping
    public void add(@RequestBody  VoteUI voteUI) {
        voteService.add(voteUI);
    }

    @GetMapping
    public Song getWinner(){
        return voteService.getWinner();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void resetVotes() {
        voteService.resetVotes();
    }
}
