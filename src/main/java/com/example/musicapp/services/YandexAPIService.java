package com.example.musicapp.services;

import com.example.musicapp.dtos.TrackDto;
import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.dtos.YandexPlaylistInfo;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexAPIService {
    @Value("${yandex.api-url}")
    private String apiUrl;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    RestTemplate restTemplate = new RestTemplate();

    HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();



    private String getUserId(String token) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://login.yandex.ru/info?"))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .GET()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String id_strip = response.body().substring(response.body().indexOf("id"));

        return id_strip.substring(6, id_strip.indexOf("\","));
    }

    public String getCurrentUser() throws IOException, InterruptedException{
        OAuthToken tokens = tokenService.getUsersTokens(tokenService.getUser(), "yandex");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://login.yandex.ru/info?"))
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .header("Accept", "application/json")
                .GET()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public List<Map<String, Object>> getUsersPlaylists() throws IOException, InterruptedException {
        log.info("yandex getUsersPlaylists");

        OAuthToken tokens = tokenService.getUsersTokens(tokenService.getUser(), "yandex");

        String uid = getUserId(tokens.getAccessToken());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "OAuth " + tokens.getAccessToken());
        headers.set("Accept", "application/json");
        headers.set("X-Yandex-Music-Client", "YandexMusicAPI");

        HttpEntity<Void> request = new HttpEntity<>(headers);


        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.music.yandex.net/users/" + uid + "/playlists/list",
                HttpMethod.GET,
                request,
                Map.class
        );


        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("result")) {
            return List.of();
        }

        return (List<Map<String, Object>>) body.get("result");
    }

    public List<TrackDto> getPlaylistTracks(String playlistId) throws IOException, InterruptedException {
        OAuthToken tokens = tokenService.getUsersTokens(tokenService.getUser(), "yandex");

        String uid = getUserId(tokens.getAccessToken());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "OAuth " + tokens.getAccessToken());
        headers.set("Accept", "application/json");
        headers.set("X-Yandex-Music-Client", "YandexMusicAPI");


        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/users/" + uid + "/playlists/" + playlistId,
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<?, ?> body = response.getBody();
        if (body == null || !body.containsKey("result")) return List.of();


        Map<String, Object> result = (Map<String, Object>) body.get("result");


        List<Map<String, Object>> tracks = (List<Map<String, Object>>) result.get("tracks");


        List<TrackDto> trackDtos = new ArrayList<>();

        for (Map<String, Object> trackItem : tracks) {
            Map<String, Object> track = (Map<String, Object>) trackItem.get("track");

            String id = (String) track.get("id");

            String title = (String) track.get("title");

            List<Map<String, Object>> artists = (List<Map<String, Object>>) track.get("artists");
            String artist = artists != null && !artists.isEmpty() ? artists.get(0).get("name").toString() : "Unknown";

            List<Map<String, Object>> albums = (List<Map<String, Object>>) track.get("albums");
            String album = albums != null && !albums.isEmpty() ? albums.get(0).get("title").toString() : "Unknown Album";

            String cover = null;
            if (albums != null && !albums.isEmpty()) {
                Map<String, Object> coverMap = (Map<String, Object>) albums.get(0).get("cover");
                if (coverMap != null) {
                    cover = "https://" + coverMap.toString().substring(coverMap.toString().indexOf("avatars")).replace("%%}", "200x200");
                }
            }

            trackDtos.add(new TrackDto(id, title, artist, album, cover));
        }

        return trackDtos;
    }

    public List<TrackDto> searchTracks(String query) throws IOException, InterruptedException {
        OAuthToken tokens = tokenService.getUsersTokens(tokenService.getUser(), "yandex");

        String url = apiUrl + "/search?text=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&page=1&type=track";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "OAuth " + tokens.getAccessToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(response.body());

        JsonNode tracksNode = root.path("result").path("tracks").path("results");

        List<TrackDto> tracks = new ArrayList<>();

        for (JsonNode item : tracksNode) {
            String trackId = item.path("id").asText();
            String title = item.path("title").asText();
            String artist = item.path("artists").get(0).path("name").asText();
            String album = item.path("albums").get(0).path("title").asText();
            String coverUrl = item.path("coverUri").asText().replace("%%", "200x200");


            tracks.add(new TrackDto(trackId, title, artist, album, coverUrl));
        }
        return tracks;
    }
}
