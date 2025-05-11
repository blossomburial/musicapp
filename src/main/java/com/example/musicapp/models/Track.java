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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "track_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "platform")
    private String platform;
}

