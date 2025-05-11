package com.example.musicapp.repositories;

import com.example.musicapp.models.ExternalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExternalServiceRepository extends JpaRepository<ExternalService, Long> {
    Optional<ExternalService> findByProvider(String provider);
}
