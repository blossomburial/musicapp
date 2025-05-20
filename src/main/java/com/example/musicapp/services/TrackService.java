package com.example.musicapp.services;

import com.example.musicapp.models.Track;
import com.example.musicapp.repositories.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrackService {
    private final TrackRepository songRepository;

//    public List<Track> listSongs(String title) {
//        if (title != null) return songRepository.findByTitle(title);
//        return songRepository.findAll();
//    }

    public void saveSong(Track track) {
        log.info("saving new {}", track);
        songRepository.save(track);
    }

    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }

    public Track getSongById(Long id) {
        return songRepository.findById(id).orElse(null);
    }
}
