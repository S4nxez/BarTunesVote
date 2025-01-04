package org.example.bartunesvote.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Song {
    private String songName;
    private String songId;
    private String songPlace;
    private BigDecimal votes;

    public void addVote() {
        this.votes = this.votes.add(BigDecimal.ONE);
    }
}
