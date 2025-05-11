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
            @RequestBody LoginDto userDto,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        authService.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword())
        );
        var userDetails = customUserDetailsService.loadUserByUsername(userDto.getUsername());
        var jwt = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new LoginResponse(jwt));

//        User authenticatedUser = authService.authenticate(loginUserDto);
//        System.out.println(authenticatedUser);
//        String jwtToken = jwtService.generateToken(authenticatedUser);
//
//        Cookie jwtCookie = new Cookie("jwt", jwtToken);
//        jwtCookie.setHttpOnly(true);
//        jwtCookie.setPath("/");
//        jwtCookie.setMaxAge((int) (jwtService.getExpirationTime() / 1000)); // в секундах
//
//        if (loginUserDto.getUsername() != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginUserDto.getUsername());
//
//            if (jwtService.isTokenValid(jwtToken, userDetails)) {
//                UsernamePasswordAuthenticationToken authToken =
//                        new UsernamePasswordAuthenticationToken(
//                                userDetails,
//                                null,
//                                userDetails.getAuthorities()
//                        );
//
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//            }
//        }



        response.addCookie(jwtCookie);

        return "redirect:/";
    }

//    @PostMapping("/logout")
//    public String logoutPage(HttpServletResponse response){
//
//        Cookie cookie = new Cookie("jwt", null);
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//        cookie.setMaxAge(0);
//
//        response.addCookie(cookie);
//
//        return "redirect:/";
//    }

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


