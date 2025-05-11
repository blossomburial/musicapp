package com.example.musicapp.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "externalservices")
public class ExternalService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;
    private String accessToken;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
