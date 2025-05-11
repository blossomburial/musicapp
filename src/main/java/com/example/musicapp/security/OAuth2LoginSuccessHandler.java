package com.example.musicapp.security;

import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.TokenRepository;
import com.example.musicapp.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    public OAuth2LoginSuccessHandler(UserRepository userRepository,
                                           TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient authorizedClient = (OAuth2AuthorizedClient) request
                .getAttribute(OAuth2AuthorizationContext.class.getName());

        if (authorizedClient == null) {
            response.sendRedirect("/login?error=oauth_client_not_found");
            return;
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

        String email = oauthToken.getPrincipal().getAttribute("email");

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("Пользователь не найден: " + email));

        // Сохраняем токен
        OAuthToken token = new OAuthToken();
        token.setProvider(oauthToken.getAuthorizedClientRegistrationId());
        token.setAccessToken(accessToken.getTokenValue());
        token.setUser(user);
        userRepository.save(user); // сохраняем пользователя и каскадно токены

        response.sendRedirect("/profile");
    }
}


