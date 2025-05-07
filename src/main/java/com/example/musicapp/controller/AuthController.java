package com.example.musicapp.controller;

import com.example.musicapp.dtos.*;
import com.example.musicapp.models.User;
import com.example.musicapp.responses.LoginResponse;
import com.example.musicapp.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.musicapp.services.AuthService;

@Controller
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;

    public AuthController(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }


    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new LoginDto());

        return "login";
    }

    @PostMapping("/login")
    public String authenticate(
            @ModelAttribute("user") LoginDto loginUserDto,
            HttpServletResponse response
    ) {
        User authenticatedUser = authService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);

        Cookie jwtCookie = new Cookie("jwt", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) (jwtService.getExpirationTime() / 1000)); // в секундах

        response.addCookie(jwtCookie);

        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logoutPage(HttpServletResponse response){

        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return "redirect:/";
    }

    @GetMapping("/signup")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new RegistrationDto());
        return "registration";
    }


    @PostMapping("/signup")
    public String registerUser(@ModelAttribute RegistrationDto registrationUserDto) {
        User registeredUser = authService.signup(registrationUserDto);
        return "redirect:/login";
    }
}


