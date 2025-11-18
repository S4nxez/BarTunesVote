package org.example.bartunesvote.ui;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.bartunesvote.domain.model.SongCard;
import org.example.bartunesvote.domain.model.VoteUI;
import org.example.bartunesvote.domain.services.VoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

@Log4j2
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/vote")
public class VoteController {
    private final VoteService voteService;

    @PostMapping
    public ResponseEntity<Object> add(@RequestBody  VoteUI voteUI) throws ResponseStatusException {
        if (!voteService.add(voteUI))
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Ya has votado"));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<SongCard> getWinner(){
        return ResponseEntity.ok(voteService.getWinner());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void resetVotes() {
        voteService.resetVotes();
    }
}
