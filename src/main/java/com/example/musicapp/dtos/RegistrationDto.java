package com.example.musicapp.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RegistrationDto {
    private String username;
    private String password;
    private String email;
    private String confirmPassword;
}
