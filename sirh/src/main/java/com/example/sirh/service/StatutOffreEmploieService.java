package com.example.sirh.service;

import com.example.sirh.model.StatutOffreEmploi;
import com.example.sirh.repository.StatutOffreEmploieRepository;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StatutOffreEmploieService {

    private final StatutOffreEmploieRepository statutOffreRepo;

    public List<StatutOffreEmploi> getAllStatuts() {
        return statutOffreRepo.findAll();
    }

    public StatutOffreEmploi getByLibelle(String libelle) {
        return statutOffreRepo.findByLibelle(libelle)
                .orElseThrow(() -> new RuntimeException("Statut " + libelle + " introuvable en base"));
    }

    public StatutOffreEmploi getById(Integer id) {
        return statutOffreRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Statut introuvable"));
    }
}