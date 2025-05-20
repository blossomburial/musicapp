package com.example.musicapp.controller;

import com.example.musicapp.models.Playlist;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.UserRepository;
import com.example.musicapp.services.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/playlist")
@RequiredArgsConstructor
public class PlaylistController {
    private final PlaylistService playlistService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<String> createPlaylist(@RequestBody Map<String, String> payload, Principal principal) {
        log.info("/api/playlist/create was called");

        String name = payload.get("name");

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(playlistService.createPlaylist(user, name));
    }

    @PostMapping("/add-track")
    public ResponseEntity<?> addTrackToPlaylist(@RequestBody Map<String, String> payload, Principal principal) {
        log.info("/api/playlist/add-track was called");

        String trackId = payload.get("trackId");
        String playlistId = payload.get("playlistId");

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        playlistService.addTrackToPlaylist(user, playlistId, trackId);
        return ResponseEntity.ok().build();
    }
}
