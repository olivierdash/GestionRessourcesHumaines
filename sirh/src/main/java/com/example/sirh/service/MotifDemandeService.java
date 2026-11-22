package com.example.sirh.service;

import com.example.sirh.model.MotifDemande;
import com.example.sirh.repository.MotifDemandeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class MotifDemandeService {

    private MotifDemandeRepository motifDemandeRepository;

    public List<MotifDemande> getAll() {
        return motifDemandeRepository.findAll();
    }

    public MotifDemande getById(Integer id) {
        return motifDemandeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Motif introuvable"));
    }
}