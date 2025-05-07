package com.example.musicapp.repositories;

import com.example.musicapp.models.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Track, Long> {
    List<Track> findByTitle(String title);
}
