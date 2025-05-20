package com.example.musicapp.repositories;

import com.example.musicapp.models.Playlist;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PlaylistRepository extends CrudRepository<Playlist, Integer> {
    Optional<Playlist> findByTitle(String playlistName);
    Optional<Playlist> findById(Long playlistID);

}
