package com.example.musicapp.dtos;

import lombok.*;

@Data
@Getter
@Setter
public class LoginDto {
    private String username;
    private String password;
}
