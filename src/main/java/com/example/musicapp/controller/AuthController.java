package com.example.musicapp.controller;

import com.example.musicapp.dtos.*;
import com.example.musicapp.models.User;
import com.example.musicapp.responses.LoginResponse;
import com.example.musicapp.services.CustomOAuth2UserService;
import com.example.musicapp.services.CustomUserDetailsService;
import com.example.musicapp.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.musicapp.services.AuthService;

@Controller
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthController(JwtService jwtService, AuthService authService, CustomUserDetailsService customUserDetailsService) {
        this.jwtService = jwtService;
        this.authService = authService;
        this.customUserDetailsService = customUserDetailsService;
    }


    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new LoginDto());

        return "login";
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(
            @ModelAttribute LoginDto authRequest,
            HttpServletResponse response
    ) {
        User user = authService.authenticate(authRequest);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        String jwt = jwtService.generateToken(userDetails);

        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtService.getExpirationTime()); // в секундах
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/profile").build();
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // удаляет cookie
        response.addCookie(cookie);

        return "redirect:/login";
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


