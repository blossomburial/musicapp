package com.example.musicapp.controller;

import com.example.musicapp.services.SpotifyAPIService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyAPIController {

    private final SpotifyAPIService spotifyService;

    @GetMapping("/me")
    public String getSpotifyMe() throws IOException, InterruptedException {
        return spotifyService.getCurrentUser();
    }

    @GetMapping("/playlists")
    public ResponseEntity<?> getUserPlaylists() {
        return ResponseEntity.ok(spotifyService.getUserPlaylists());
    }

}
