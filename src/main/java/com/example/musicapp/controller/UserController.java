package com.example.musicapp.controller;

import com.example.musicapp.dtos.TrackDto;
import com.example.musicapp.repositories.UserRepository;
import com.example.musicapp.models.User;
import com.example.musicapp.services.SpotifyAPIService;
import com.example.musicapp.services.YandexAPIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final SpotifyAPIService spotifyService;
    private final YandexAPIService yandexService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        model.addAttribute("user", user);
        model.addAttribute("tokens", user.getTokens());
        return "profile";
    }

    @GetMapping("/profile/settings")
    public String settingsPage(){
        return "settings";
    }

    @GetMapping("/playlist/{id}")
    public String showPlaylistTracks(@PathVariable("id") String playlistId, @RequestParam("platform") String platform, Model model) throws IOException, InterruptedException {
        log.info("/playlist/id was called");

        List<TrackDto> tracks = switch (platform.toLowerCase()) {
            case "spotify" -> spotifyService.getPlaylistTracks(playlistId);
            case "yandex" -> yandexService.getPlaylistTracks(playlistId);
            default -> List.of();
        };

        model.addAttribute("tracks", tracks);

        return "playlist";
    }



}
