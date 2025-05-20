package com.example.musicapp.services;

import com.example.musicapp.models.Playlist;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;

    public void addTrackToPlaylist(User user, String playlistID, String trackID) {

    }

    public String createPlaylist(User user, String playlistName) {
        Playlist playlist = new Playlist();
        if (playlistRepository.findByTitle(playlistName).isEmpty()) {
            playlist.setTitle(playlistName);
            playlist.setAuthor(user);

            playlistRepository.save(playlist);

            return "playlist created";
        }
        else {
            return "playlist with this name already exist";
        }
    }
}
