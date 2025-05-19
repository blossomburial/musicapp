package com.example.musicapp.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Data
@Getter
@Setter
public class SpotifyPlaylistInfo {
    private String id;
    private String name;
    private Tracks tracks;

    @Data
    @Setter
    @Getter
    public static class Tracks {
        private List<Item> items;

        @Data
        @Setter
        @Getter
        public static class Item {
            private Track track;

            @Data
            @Setter
            @Getter
            public static class Track {
                private String id;
                private String name;
                private int duration_ms;
                private List<Artist> artists;
                private boolean explicit;
                private String preview_url;

                @Data
                @Getter
                @Setter
                public static class Artist {
                    private String name;
                }
            }
        }
    }
}