package com.example.musicapp.dtos;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackDto {
    private String id;
    private String title;
    private String artist;
    private String album;
    private String coverUrl;
}
