package com.example.sirh.service;

import com.example.sirh.model.Lieu;
import com.example.sirh.repository.LieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LieuService {

    private final LieuRepository lieuRepository;

    @Transactional(readOnly = true)
    public List<Lieu> getAll() {
        return lieuRepository.findAllByOrderByLibelleAsc();
    }

    @Transactional(readOnly = true)
    public Lieu getById(Integer id) {
        return lieuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lieu introuvable : " + id));
    }

    @Transactional
    public Lieu creer(String libelle) {
        if (libelle == null || libelle.isBlank()) {
            throw new IllegalArgumentException("Le libellé est obligatoire.");
        }
        String libelleNorm = libelle.trim();
        if (lieuRepository.existsByLibelleIgnoreCase(libelleNorm)) {
            throw new IllegalArgumentException("Un lieu avec ce libellé existe déjà.");
        }
        return lieuRepository.save(Lieu.builder()
                .libelle(libelleNorm)
                .build());
    }

    @Transactional
    public Lieu modifier(Integer id, String libelle) {
        Lieu lieu = getById(id);
        if (libelle == null || libelle.isBlank()) {
            throw new IllegalArgumentException("Le libellé est obligatoire.");
        }
        String libelleNorm = libelle.trim();
        if (lieuRepository.existsByLibelleIgnoreCaseAndIdLieuNot(libelleNorm, id)) {
            throw new IllegalArgumentException("Un autre lieu porte déjà ce libellé.");
        }
        lieu.setLibelle(libelleNorm);
        return lieu;
    }
}