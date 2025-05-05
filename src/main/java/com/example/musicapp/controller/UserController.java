package com.example.musicapp.controller;

import com.example.musicapp.dtos.RegistrationDto;
import com.example.musicapp.dtos.LoginDto;
import com.example.musicapp.services.AuthService;
import com.example.musicapp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/signup")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new RegistrationDto());
        return "registration";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, LoginDto loginDto) {
        model.addAttribute("user", new LoginDto());
        return "login"; //
    }
//
//    @GetMapping("/profile")
//    public String profile(Principal principal,
//                          Model model) {
//        User user = userService.getUserByPrincipal(principal);
//        model.addAttribute("user", user);
//        return "profile";
//    }

}
