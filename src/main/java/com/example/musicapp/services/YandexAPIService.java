package com.example.musicapp.services;

import com.example.musicapp.dtos.PlaylistInfo;
import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.dtos.YandexPlaylistInfo;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YandexAPIService {
    @Value("${yandex.api-url}")
    private String apiUrl;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    RestTemplate restTemplate = new RestTemplate();

    private String getUserId(String token) throws IOException, InterruptedException{

        HttpClient client = HttpClient.newHttpClient();
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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Optional<OAuthToken> yandexTokens = tokenRepository.findByUserAndProvider(user, "yandex");

        String accessToken = yandexTokens.get().getAccessToken();
        String refreshToken = yandexTokens.get().getRefreshToken();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://login.yandex.ru/info?"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    public List<Map<String, Object>> getUsersPlaylists() throws IOException, InterruptedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Optional<OAuthToken> yandexTokens = tokenRepository.findByUserAndProvider(user, "yandex");

        String accessToken = yandexTokens.get().getAccessToken();
        String refreshToken = yandexTokens.get().getRefreshToken();

        String uid = getUserId(accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "OAuth " + accessToken);
        headers.set("Accept", "application/json");

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

    public List<PlaylistInfo> getPlaylistTracks(String playlistId) throws IOException, InterruptedException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<OAuthToken> yandexTokens = tokenRepository.findByUserAndProvider(user, "yandex");

        String accessToken = yandexTokens.get().getAccessToken();
        String refreshToken = yandexTokens.get().getRefreshToken();

        String uid = getUserId(accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "OAuth " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<YandexPlaylistInfo> response = restTemplate.exchange(
                apiUrl + "/users/" + uid + "/playlists/" + playlistId,
                HttpMethod.GET,
                request,
                YandexPlaylistInfo.class
        );

        List<PlaylistInfo> yandexTracks = Objects.requireNonNull(response.getBody())
                .getTracks().stream()
                .map(this::convertYandexTrack)
                .collect(Collectors.toList());

        System.out.println(yandexTracks);

        return null;
//        return yandexTracks;
    }

    public PlaylistInfo convertYandexTrack(YandexPlaylistInfo.Track yandexTrack) {
        PlaylistInfo dto = new PlaylistInfo();
        dto.setId(String.valueOf(yandexTrack.getId()));
        dto.setTitle(yandexTrack.getTitle());
        dto.setArtist(yandexTrack.getArtists().get(0).getName());
        dto.setDurationSec(yandexTrack.getDurationMs() / 1000);
        return dto;
    }
}
