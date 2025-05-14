package com.example.musicapp.services;

import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import lombok.*;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpotifyAPIService {
    @Value("${spotify.api-url}")
    private String apiUrl;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;


    public String getCurrentUser() throws IOException, InterruptedException {

        System.out.println(">>> Метод getCurrentUser() вызван");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Optional<OAuthToken> spotifyTokens = tokenRepository.findByUserAndProvider(user, "spotify");

        String accessToken = spotifyTokens.get().getAccessToken();
        String refreshToken = spotifyTokens.get().getRefreshToken();

        System.out.println(accessToken);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/me"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Ответ Spotify API:");
        System.out.println(response.body());

        return response.body();
    }

    public List<Map<String, Object>> getUserPlaylists() {
        RestTemplate restTemplate = new RestTemplate();
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

}
