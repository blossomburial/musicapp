package com.example.musicapp.security;

import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public OAuth2LoginSuccessHandler(UserRepository userRepository,
                                     TokenRepository tokenRepository,
                                     OAuth2AuthorizedClientService authorizedClientService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (!(currentAuth.getPrincipal() instanceof User user)) {
            response.sendRedirect("/login?error=unauthorized");
            return;
        }

        // Получение токена через OAuth2AuthorizedClientService
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );

        if (client == null) {
            response.sendRedirect("/login?error=oauth_client_not_found");
            return;
        }

        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String accessToken = client.getAccessToken().getTokenValue();

        OAuthToken token = new OAuthToken();
        token.setProvider(provider);
        token.setAccessToken(accessToken);
        token.setUser(user);

        tokenRepository.save(token);

        response.sendRedirect("/profile/settings");
    }
}



