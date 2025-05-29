package com.example.musicapp.controller;

import com.example.musicapp.repositories.UserRepository;
import com.example.musicapp.services.SpotifyAPIService;
import com.example.musicapp.services.YandexAPIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
    private final SpotifyAPIService spotifyService;
    private final YandexAPIService yandexService;

    @GetMapping("/")
    public String playlists(Model model)  throws IOException, InterruptedException{
        log.info("/playlists was called");

        List<Map<String, Object>> spotifyPlaylists = spotifyService.getUsersPlaylists();
        List<Map<String, Object>> yandexPlaylists = yandexService.getUsersPlaylists();

        model.addAttribute("spotifyPlaylists", spotifyPlaylists);
        model.addAttribute("yandexPlaylists", yandexPlaylists);

        return "playlists";
    }

    @GetMapping("/search")
    public String search() { return "search"; }

}
