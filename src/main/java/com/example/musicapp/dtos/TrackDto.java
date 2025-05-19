package com.example.musicapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class TrackDto {
    private String id;
    private String title;
    private String artist;
    private String album;
    private String coverUrl;
}
