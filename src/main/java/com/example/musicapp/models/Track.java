package com.example.musicapp.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="songs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Track {
    @Id
    @Column(name = "track_id")
    private String id;

    @Column(name = "track_url")
    private String trackURL;

    @Column(name = "platform")
    private String platform;
}

