package com.example.musicapp.controller;

import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OAuthController {

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

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    RestTemplate restTemplate = new RestTemplate();

//    @GetMapping("/oauth2/refresh/spotify")
//    public String handleSpotifyRefreshCallback(String code) {
//
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        if (auth == null || !auth.isAuthenticated()) {
//            return "redirect:/login?error=NoAuth";
//        }
//
//        User currentUser = userRepository.findByUsername(auth.getName())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//
//        Optional<OAuthToken> existingTokenOpt = tokenRepository.findByUserAndProvider(currentUser, "spotify");
//        String refreshToken;
//        OAuthToken token = existingTokenOpt.get();
//        refreshToken = token.getRefreshToken();
//
//        System.out.println(refreshToken);
//
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("grant_type", "refresh_token");
//        params.add("refresh_token", refreshToken);
//        params.add("client_id", spotifyClientId);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                "https://accounts.spotify.com/api/token",
//                params,
//                Map.class
//        );
//
//        if (!response.getStatusCode().is2xxSuccessful()) {
//            return "redirect:/profile/settings?error=TokenError";
//        }
//
//        Map<String, Object> tokenData = response.getBody();
//        String accessToken = (String) tokenData.get("access_token");
//        refreshToken = (String) tokenData.get("refresh_token");
//        Integer expiresIn = (Integer) tokenData.get("expires_in");
//
//        token.setUser(currentUser);
//        token.setProvider("spotify");
//        token.setAccessToken(accessToken);
//        token.setRefreshToken(refreshToken);
//        token.setExpiresIn(expiresIn);
//        tokenRepository.save(token);
//
//        return "redirect:/profile";
//    }

    @GetMapping("/oauth2/authorize/spotify")
    public void redirectToSpotify(HttpServletResponse response) throws IOException {
        String url = "https://accounts.spotify.com/authorize"
                + "?client_id=" + spotifyClientId
                + "&response_type=code"
                + "&redirect_uri=" + spotifyRedirectUri;
        response.sendRedirect(url);
    }

    @GetMapping("/oauth2/callback/spotify")
    public String handleSpotifyCallback(String code) {
        RestTemplate restTemplate = new RestTemplate();

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
            return "redirect:/profile/settings?error=TokenError";
        }

        Map<String, Object> tokenData = response.getBody();
        String accessToken = (String) tokenData.get("access_token");
        String refreshToken = (String) tokenData.get("refresh_token");
        Integer expiresIn = (Integer) tokenData.get("expires_in");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?error=NoAuth";
        }

        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));


        Optional<OAuthToken> existingTokenOpt = tokenRepository.findByUserAndProvider(currentUser, "spotify");

        OAuthToken token;
        if (existingTokenOpt.isPresent()) {
            token = existingTokenOpt.get();
            token.setAccessToken(accessToken);
            if (refreshToken != null) token.setRefreshToken(refreshToken);
            token.setExpiresIn(expiresIn);
        } else {
            token = new OAuthToken();
            token.setUser(currentUser);
            token.setProvider("spotify");
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresIn(expiresIn);
        }

        tokenRepository.save(token);

        return "redirect:/profile";
    }

    @GetMapping("/oauth2/authorize/yandex")
    public void redirectToYandex(HttpServletResponse response) throws IOException {
        String url = "https://oauth.yandex.ru/authorize"
                + "?response_type=code"
                + "&client_id=" + yandexClientId;
        response.sendRedirect(url);
    }

    @GetMapping("/oauth2/callback/yandex")
    public String handleYandexCallback(String code) {
        String tokenUrl = "https://oauth.yandex.ru/authorize";

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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?error=NoAuth";
        }

        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));


        Optional<OAuthToken> existingTokenOpt = tokenRepository.findByUserAndProvider(currentUser, "yandex");

        OAuthToken token;
        if (existingTokenOpt.isPresent()) {
            token = existingTokenOpt.get();
            token.setAccessToken(accessToken);
            if (refreshToken != null) token.setRefreshToken(refreshToken);
            token.setExpiresIn(expiresIn);
        } else {
            token = new OAuthToken();
            token.setUser(currentUser);
            token.setProvider("yandex");
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresIn(expiresIn);
        }

        tokenRepository.save(token);

        return "redirect:/profile";
    }

}
