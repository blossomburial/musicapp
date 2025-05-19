package com.example.musicapp.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class PlaylistInfo {
    private String id;
    private String title;
    private String artist;
    private int durationSec;
}
