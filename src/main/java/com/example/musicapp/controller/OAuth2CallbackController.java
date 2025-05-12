package com.example.musicapp.controller;

import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/login/oauth2/callback")
public class OAuth2CallbackController {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    public OAuth2CallbackController(OAuth2AuthorizedClientService authorizedClientService,
                                    TokenRepository tokenRepository,
                                    UserRepository userRepository) {
        this.authorizedClientService = authorizedClientService;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/spotify")
    public String spotifyCallback(@AuthenticationPrincipal User currentUser,
                                  OAuth2AuthenticationToken authentication) {

        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName()
                );

        if (client == null) {
            return "redirect:/profile/settings?error=client_not_found";
        }

        String provider = authentication.getAuthorizedClientRegistrationId();

        OAuth2AccessToken accessToken = client.getAccessToken();

        Optional<OAuthToken> existing = tokenRepository.findByUserAndProvider(currentUser, provider);

        OAuthToken token = existing.orElse(new OAuthToken());
        token.setUser(currentUser);
        token.setProvider(provider);
        token.setAccessToken(accessToken.getTokenValue());
        token.setExpiresAt(accessToken.getExpiresAt());

        tokenRepository.save(token);

        return "redirect:/profile/settings?connected=" + provider;
    }
}

