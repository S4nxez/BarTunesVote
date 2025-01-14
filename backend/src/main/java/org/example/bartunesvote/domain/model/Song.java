package org.example.bartunesvote.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Song {
    private String songName;
    private String artistName;
    private String songId;
    private String place;
}
