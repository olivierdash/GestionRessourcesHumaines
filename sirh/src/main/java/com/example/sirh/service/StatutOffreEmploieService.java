package com.example.sirh.service;

import com.example.sirh.model.StatutOffreEmploi;
import com.example.sirh.repository.StatutOffreEmploieRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StatutOffreEmploieService {

    private final StatutOffreEmploieRepository statutOffreRepo;

    public StatutOffreEmploi getByLibelle(String libelle) {
        return statutOffreRepo.findByLibelle(libelle)
                .orElseThrow(() -> new RuntimeException("Statut " + libelle + " introuvable en base"));
    }

    public StatutOffreEmploi getById(Integer id) {
        return statutOffreRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Statut introuvable"));
    }
}