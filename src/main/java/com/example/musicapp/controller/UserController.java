package com.example.musicapp.controller;

import com.example.musicapp.dtos.RegistrationDto;
import com.example.musicapp.dtos.LoginDto;
import com.example.musicapp.models.ExternalService;
import com.example.musicapp.repositories.UserRepository;
import com.example.musicapp.repositories.ExternalServiceRepository;
import com.example.musicapp.models.User;
import com.example.musicapp.services.AuthService;
import com.example.musicapp.services.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final ExternalServiceRepository externalServiceRepository;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal){

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {
            model.addAttribute("username", user.getUsername());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("accountLinked", user.getTokens() != null);
        }
        return "profile";
    }



    @GetMapping("/profile/settings")
    public String settingsPage(){
        return "settings";
    }

}
