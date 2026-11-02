package com.example.sirh.service;

import com.example.sirh.model.Candidat;
import com.example.sirh.repository.CandidatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CandidatService {

    private final CandidatRepository candidatRepository;

    public Candidat getCandidatById(Integer id) {
        return candidatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidat introuvable"));
    }
}