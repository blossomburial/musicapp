package com.example.musicapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.musicapp.models.Role;
import com.example.musicapp.models.User;
import com.example.musicapp.repositories.RoleRepository;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role getUserRole() {
        return roleRepository.findByName("ROLE_USER").get();
    }
}
