package com.example.musicapp.controller;

import com.example.musicapp.dtos.TrackDto;
import com.example.musicapp.repositories.UserRepository;
import com.example.musicapp.services.SpotifyAPIService;
import com.example.musicapp.services.YandexAPIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {
    private final SpotifyAPIService spotifyService;
    private final YandexAPIService yandexService;

    @GetMapping("/search")
    public List<?> searchTracks (@RequestParam("query") String query) throws IOException, InterruptedException {
        List<TrackDto> spotifySearch = spotifyService.searchTracks(query);
        List<TrackDto> yandexSearch = yandexService.searchTracks(query);

        List<TrackDto> tracks = Stream.concat(spotifySearch.stream(), yandexSearch.stream()).toList();

        System.out.println(tracks);
        return tracks;
    }
}
