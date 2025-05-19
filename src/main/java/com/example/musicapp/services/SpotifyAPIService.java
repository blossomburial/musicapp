package com.example.musicapp.services;

import com.example.musicapp.dtos.TrackDto;
import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.dtos.SpotifyPlaylistInfo;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
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
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpotifyAPIService {
    @Value("${spotify.api-url}")
    private String apiUrl;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    RestTemplate restTemplate = new RestTemplate();

    private String getUserId(String token) throws IOException, InterruptedException{
        ProxySelector proxySelector = ProxySelector.of(new InetSocketAddress("34.216.237.152", 3128));

        HttpClient client = HttpClient.newBuilder()
                .proxy(proxySelector)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

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


    public String getCurrentUser() throws IOException, InterruptedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Optional<OAuthToken> spotifyTokens = tokenRepository.findByUserAndProvider(user, "spotify");

        String accessToken = spotifyTokens.get().getAccessToken();
        String refreshToken = spotifyTokens.get().getRefreshToken();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/me"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public List<Map<String, Object>> getUsersPlaylists() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OAuthToken token = tokenRepository.findByUserAndProvider(user, "spotify")
                .orElseThrow(() -> new RuntimeException("Spotify token not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.getAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.spotify.com/v1/me/playlists",
                HttpMethod.GET,
                request,
                Map.class
        );



        Map<String, Object> body = response.getBody();


        if (body == null || !body.containsKey("items")) {
            return List.of();
        }

        return (List<Map<String, Object>>) body.get("items");
    }

    public List<TrackDto> getPlaylistTracks(String playlistId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OAuthToken token = tokenRepository.findByUserAndProvider(user, "spotify")
                .orElseThrow(() -> new RuntimeException("Spotify token not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
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
}
