package com.example.musicapp.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name="playlists")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @ManyToMany
    @JoinTable(
            name = "playlist_track",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "track_id")
    )
    private Set<Track> tracks;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;
}
