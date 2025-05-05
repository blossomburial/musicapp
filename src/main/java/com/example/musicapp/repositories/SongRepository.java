package com.example.musicapp.repositories;

import com.example.musicapp.models.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Track, Long> {
    List<Track> findByTitle(String title);
}
