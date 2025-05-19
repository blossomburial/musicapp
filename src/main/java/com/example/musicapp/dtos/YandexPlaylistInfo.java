package com.example.musicapp.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class YandexPlaylistInfo {
    private String kind;
    private String title;
    private List<Track> tracks;

    @Data
    @Getter
    @Setter
    public static class Track {
        private String id;
        private String title;
        private List<Artist> artists;
        private int durationMs;

        @Data
        @Getter
        @Setter
        public static class Artist {
            private String name;
        }
    }
}
