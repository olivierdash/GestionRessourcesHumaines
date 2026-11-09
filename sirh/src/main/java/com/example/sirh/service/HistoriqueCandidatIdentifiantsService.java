package com.example.sirh.service;

import com.example.sirh.model.Candidat;
import com.example.sirh.model.HistoriqueCandidatIdentifiants;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.HistoriqueCandidatIdentifiantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoriqueCandidatIdentifiantsService {

    private final HistoriqueCandidatIdentifiantsRepository repo;

    @Transactional
    public void enregistrer(Candidat candidat) {
        if(candidat == null){
            throw new IllegalArgumentException("Le candidat doit exister pour pouvoir modifier ces informations");
        }
        repo.save(HistoriqueCandidatIdentifiants.builder()
                .candidat(candidat)
                .ancienNom(candidat.getNom())
                .ancienPrenom(candidat.getPrenom())
                .ancienEmail(candidat.getEmail())
                .build());
    }

    @Transactional(readOnly = true)
    public List<HistoriqueCandidatIdentifiants> listerPourCandidat(Integer idCandidat) {
        return repo.findByCandidatIdCandidatOrderByDateChangementDesc(idCandidat);
    }
}