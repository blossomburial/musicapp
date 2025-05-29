package com.example.musicapp.services;

import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {
    @Value("${spotify.client-id}")
    private String spotifyClientId;

    @Value("${spotify.client-secret}")
    private String spotifyClientSecret;

    @Value("${spotify.redirect-uri}")
    private String spotifyRedirectUri;

    @Value("${yandex.client-id}")
    private String yandexClientId;

    @Value("${yandex.client-secret}")
    private String yandexClientSecret;

    @Value("${yandex.redirect-uri}")
    private String yandexRedirectUri;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    RestTemplate restTemplate = new RestTemplate();

    public User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public OAuthToken getUsersTokens(User user, String provider) {
        return tokenRepository.findByUserAndProvider(user, provider)
                .orElseThrow(() -> new RuntimeException("No " + provider + " token found for user"));
    }

    public String getNewSpotifyToken() {
        OAuthToken token = getUsersTokens(getUser(), "spotify");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", token.getRefreshToken());
        body.add("client_id", spotifyClientId);
        body.add("client_secret", spotifyClientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("https://accounts.spotify.com/api/token", request, Map.class);

        token.setAccessToken((String) response.getBody().get("access_token"));
        if (response.getBody().containsKey("refresh_token")) {
            token.setRefreshToken((String) response.getBody().get("refresh_token"));
        }
        token.setExpiresIn((Integer) response.getBody().get("expires_in"));

        tokenRepository.save(token);

        if (response.getStatusCode() == HttpStatus.OK) {
            return "token refreshed";
        } else {
            throw new RuntimeException("Failed to refresh token: " + response.getStatusCode());
        }
    }

    public String getSpotifyTokens(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", spotifyRedirectUri);
        params.add("client_id", spotifyClientId);
        params.add("client_secret", spotifyClientSecret);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://accounts.spotify.com/api/token",
                params,
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            return "redirect:/profile?error";
        }

        Map<String, Object> tokenData = response.getBody();
        String accessToken = (String) tokenData.get("access_token");
        String refreshToken = (String) tokenData.get("refresh_token");
        Integer expiresIn = (Integer) tokenData.get("expires_in");

        User user = getUser();
        OAuthToken token = getUsersTokens(user, "spotify");

        if (token != null) {
            token.setAccessToken(accessToken);
            if (refreshToken != null) token.setRefreshToken(refreshToken);
            token.setExpiresIn(expiresIn);
        } else {
            token = new OAuthToken();
            token.setUser(user);
            token.setProvider("spotify");
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresIn(expiresIn);
        }

        tokenRepository.save(token);

        return "redirect:/profile";
    }

    public String getYandexTokens(String code){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", yandexClientId);
        params.add("client_secret", yandexClientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("https://oauth.yandex.ru/token", request, Map.class);

        Map<String, Object> tokenData = response.getBody();
        String accessToken = (String) tokenData.get("access_token");
        String refreshToken = (String) tokenData.get("refresh_token");
        Integer expiresIn = (Integer) tokenData.get("expires_in");

        User user = getUser();
        OAuthToken tokens = getUsersTokens(user, "yandex");

        if (tokens != null) {
            tokens.setAccessToken(accessToken);
            if (refreshToken != null) tokens.setRefreshToken(refreshToken);
            tokens.setExpiresIn(expiresIn);
        } else {
            tokens = new OAuthToken();
            tokens.setUser(user);
            tokens.setProvider("yandex");
            tokens.setAccessToken(accessToken);
            if (refreshToken != null) tokens.setRefreshToken(refreshToken);
            tokens.setExpiresIn(expiresIn);
        }

        tokenRepository.save(tokens);

        return "redirect:/profile";
    }
}
