package org.example.bartunesvote.ui;

import lombok.extern.log4j.Log4j2;
import org.example.bartunesvote.domain.model.Song;
import org.example.bartunesvote.domain.services.VoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.bartunesvote.domain.model.VoteUI;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

@Log4j2
@RestController
@RequestMapping("/api/vote")
public class ControllerVote {
    private final VoteService voteService;

    public ControllerVote(VoteService voteService){
        this.voteService=voteService;
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestBody  VoteUI voteUI) throws ResponseStatusException {
        if (!voteService.add(voteUI)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Ya has votado"));
        }
        log.info("Voto recibido: " + voteUI);
        return ResponseEntity.ok().build();
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
