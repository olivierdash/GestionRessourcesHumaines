package com.example.sirh.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.example.sirh.model.Poste;
import com.example.sirh.repository.PosteRepository;

@Service
public class PosteService {
    private PosteRepository posteRepository;

    public PosteService(PosteRepository posteRepository) {
        this.posteRepository = posteRepository;
    }

    public Poste getById(Integer id) {
        return posteRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Poste introuvable"));
    }

    public List<Poste> getAllPostes() {
        return posteRepository.findAll();
    }
}
