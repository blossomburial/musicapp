package com.example.musicapp.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "title")
    private String title;
    @Column(name = "album")
    private String album;
    @Column(name = "length")
    private int length;
    @Column(name = "releasedate")
    private String releasedate;
    @Column(name = "listenerscnt")
    private String listenerscnt;
    @Column(name = "platform")
    private String platform;
    @Column(name = "author")
    private String author;
}

