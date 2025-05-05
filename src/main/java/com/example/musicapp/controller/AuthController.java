package com.example.musicapp.controller;

import com.example.musicapp.dtos.*;
import com.example.musicapp.models.User;
import com.example.musicapp.responses.LoginResponse;
import com.example.musicapp.services.JwtService;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.musicapp.services.AuthService;

@RequestMapping("/api/auth")
@RestController
public class AuthController {
    private final JwtService jwtService;

    private final AuthService authService;

    public AuthController(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@ModelAttribute("user") LoginDto loginDto) {
        User authenticatedUser = authService.authenticate(loginDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<User> registerUser(@ModelAttribute RegistrationDto registrationUserDto) {
        User registeredUser = authService.signup(registrationUserDto);
        return ResponseEntity.ok(registeredUser);
    }

}

