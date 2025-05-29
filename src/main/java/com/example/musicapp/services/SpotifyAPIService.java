package com.example.musicapp.services;

import com.example.musicapp.dtos.TrackDto;
import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyAPIService {
    @Value("${spotify.api-url}")
    private String apiUrl;

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    RestTemplate restTemplate = new RestTemplate();

    HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private String getAccessToken() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OAuthToken token = tokenRepository.findByUserAndProvider(user, "spotify")
                .orElseThrow(() -> new RuntimeException("Spotify token not found"));

        return token.getAccessToken();
    }

    private String getUserId(String token) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/me"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String id_strip = response.body().substring(response.body().indexOf("id"));

        return id_strip.substring(5, id_strip.indexOf("\","));
    }

    public List<Map<String, Object>> getUsersPlaylists() {
        log.info("getUsersPlaylists was called");

        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.spotify.com/v1/me/playlists",
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<?, ?> body = response.getBody();

            if (body == null || !body.containsKey("items")) {
                return List.of();
            }

            return (List<Map<String, Object>>) body.get("items");

        } catch (HttpClientErrorException.Unauthorized ex) {
            log.warn("Token expired, refreshing token...");
            tokenService.getNewSpotifyToken();

            return getUsersPlaylists();
        } catch (RestClientException ex) {
            log.error("Failed to fetch playlists: {}", ex.getMessage());
        }

        return List.of();
    }

    public List<TrackDto> getPlaylistTracks(String playlistId) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks",
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<?, ?> body = response.getBody();

        if (body == null || !body.containsKey("items")) return List.of();

        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

        List<TrackDto> result = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Map<String, Object> track = (Map<String, Object>) item.get("track");
            String id = (String) track.get("id");
            String title = (String) track.get("name");
            String artist = ((Map<String, Object>) ((List<?>) track.get("artists")).get(0)).get("name").toString();
            String album = ((Map<String, Object>) track.get("album")).get("name").toString();
            String coverUrl = ((Map<String, Object>) ((List<?>) ((Map<?, ?>) track.get("album")).get("images")).get(0)).get("url").toString();

            result.add(new TrackDto(id, title, artist, album, coverUrl));
        }
        return result;
    }

    public List<TrackDto> searchTracks(String query) throws IOException, InterruptedException {
        String token = getAccessToken();

        String url = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&type=track";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(response.body());

        JsonNode tracksNode = root.path("tracks").path("items");

        List<TrackDto> tracks = new ArrayList<>();

        for (JsonNode item : tracksNode) {
            String trackId = item.path("id").asText();
            String title = item.path("name").asText();
            String artist = item.path("artists").get(0).path("name").asText();
            String album = item.path("album").path("name").asText();
            String coverUrl = "";


            tracks.add(new TrackDto(trackId, title, artist, album, coverUrl));

        }
        return tracks;
    }
}
