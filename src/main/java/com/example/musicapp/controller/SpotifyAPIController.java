package com.example.musicapp.controller;

import com.example.musicapp.services.SpotifyAPIService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyAPIController {

    private final SpotifyAPIService spotifyService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentSpotifyUser() {
        Map<String, Object> userProfile = spotifyService.getCurrentUser();
        return ResponseEntity.ok(userProfile);
    }
}
