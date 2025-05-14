package com.example.musicapp.services;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpotifyAPIService {
    @Value("${spotify.api-url}")
    private String apiUrl;


}
