package com.example.musicapp.controller;

import com.example.musicapp.dtos.RegistrationDto;
import com.example.musicapp.dtos.LoginDto;
import com.example.musicapp.models.ExternalService;
import com.example.musicapp.repositories.UserRepository;
import com.example.musicapp.repositories.ExternalServiceRepository;
import com.example.musicapp.models.User;
import com.example.musicapp.services.AuthService;
import com.example.musicapp.services.SpotifyAPIService;
import com.example.musicapp.services.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final ExternalServiceRepository externalServiceRepository;
    private final UserRepository userRepository;
    private final SpotifyAPIService spotifyService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        model.addAttribute("user", user);
        model.addAttribute("tokens", user.getTokens());
        return "profile";
    }

    @PostMapping("/profile/unlink/{provider}")
    public String unlinkProvider(@PathVariable String provider, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        userRepository.save(user);

        return "redirect:/profile";
    }

    @GetMapping("/profile/settings")
    public String settingsPage(){
        return "settings";
    }
    @GetMapping("/playlists")
    public String playlists(Model model) {
        List<Map<String, Object>> playlists = spotifyService.getUserPlaylists();
        model.addAttribute("playlists", playlists);
        return "playlists";
    }

}
