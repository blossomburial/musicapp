package com.example.musicapp.controller;

import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class OAuthController {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @GetMapping("/oauth2/authorize/spotify")
    public void redirectToSpotify(HttpServletResponse response) throws IOException {
        String url = "https://accounts.spotify.com/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + redirectUri;
        response.sendRedirect(url);
    }

    @GetMapping("/oauth2/callback/spotify")
    public String handleSpotifyCallback(String code) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

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

}
