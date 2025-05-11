package com.example.musicapp.exceptions;

public class InvalidPassword extends Exception {
    public InvalidPassword(){
        super("passwords do not match");
    }
}
