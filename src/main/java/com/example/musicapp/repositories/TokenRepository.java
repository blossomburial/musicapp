package com.example.musicapp.repositories;

import com.example.musicapp.models.OAuthToken;
import com.example.musicapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<OAuthToken, Long> {
    Optional<OAuthToken> findByUserAndProvider(User user, String provider);
}


