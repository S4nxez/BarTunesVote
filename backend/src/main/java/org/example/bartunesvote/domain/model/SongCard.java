package org.example.bartunesvote.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
@Getter
@AllArgsConstructor
public class SongCard {
    private String place;
    private  BigDecimal votes;

    public void addVote() {
        this.votes = this.votes.add(BigDecimal.ONE);
    }
}
