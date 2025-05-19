package com.example.musicapp.controller;

import com.example.musicapp.services.YandexAPIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/yandex")
@RequiredArgsConstructor
public class YandexAPIController {
    private final YandexAPIService yandexService;

    @GetMapping("/me")
    public String getYandexMe() throws IOException, InterruptedException {
        return yandexService.getCurrentUser();
    }

    @GetMapping("/playlists")
    public ResponseEntity<?> getUserPlaylists() throws IOException, InterruptedException {
        return ResponseEntity.ok(yandexService.getUsersPlaylists());
    }

    @GetMapping("/playlist/{id}")
    public ResponseEntity<?> getPlaylistTracks(String playlistId) throws IOException, InterruptedException {
        return ResponseEntity.ok(yandexService.getPlaylistTracks(playlistId));
    }
}
