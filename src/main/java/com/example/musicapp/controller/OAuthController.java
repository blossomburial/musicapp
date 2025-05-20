package com.example.musicapp.controller;

import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import com.example.musicapp.services.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
    private final TokenService tokenService;

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/oauth2/authorize/spotify")
    public void redirectToSpotify(HttpServletResponse response) throws IOException {
        String url = "https://accounts.spotify.com/authorize"
                + "?client_id=" + spotifyClientId
                + "&response_type=code"
                + "&redirect_uri=" + spotifyRedirectUri;
        response.sendRedirect(url);
    }

    @GetMapping("/oauth2/callback/spotify")
    public String handleSpotifyCallback(String code) { return tokenService.getSpotifyTokens(code); }

    @PostMapping("/oauth2/refresh/spotify")
    public void getNewToken() { tokenService.getNewSpotifyToken(); }

    @GetMapping("/oauth2/authorize/yandex")
    public void redirectToYandex(HttpServletResponse response) throws IOException {
        String url = "https://oauth.yandex.ru/authorize"
                + "?response_type=code"
                + "&client_id=" + yandexClientId;
        response.sendRedirect(url);
    }

    @GetMapping("/oauth2/callback/yandex")
    public String handleYandexCallback(String code) { return tokenService.getYandexTokens(code); }

}
