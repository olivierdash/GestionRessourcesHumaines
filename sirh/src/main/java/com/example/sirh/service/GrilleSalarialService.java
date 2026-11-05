package com.example.sirh.service;

import com.example.sirh.dto.GrilleSalarialFilter;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.repository.GrilleSalarialRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class GrilleSalarialService {

    private final GrilleSalarialRepository grilleRepository;

    @Transactional(readOnly = true)
    public List<GrilleSalarial> lister() {
        return grilleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public GrilleSalarial trouver(Integer id) {
        return grilleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grille salariale introuvable : " + id));
    }

    public GrilleSalarial creer(GrilleSalarial grille) {
        if (grille == null) {
            throw new IllegalArgumentException("Grille salariale requise.");
        }

        validerGrille(grille);

        Integer idPoste = grille.getPoste().getIdPoste();
        Integer idDepartement = grille.getPoste().getDepartement().getIdDepartement();

        if (grilleRepository.existsByPosteIdPosteAndDepartementIdDepartement(idPoste, idDepartement)) {
            throw new IllegalArgumentException("Une grille existe déjà pour ce poste et ce département.");
        }

        grille.setDepartement(grille.getPoste().getDepartement());

        return grilleRepository.save(grille);
    }

    public GrilleSalarial modifier(GrilleSalarial grille) {
        if (grille == null) {
            throw new IllegalArgumentException("Grille salariale requise.");
        }

        validerGrille(grille);

        if (grilleRepository.existsByPosteIdPosteAndDepartementIdDepartementAndIdGrilleNot(
                grille.getPoste().getIdPoste(),
                grille.getPoste().getDepartement().getIdDepartement(),
                grille.getIdGrille())) {
            throw new IllegalArgumentException("Une autre grille existe déjà pour ce poste et ce département.");
        }

        GrilleSalarial existing = trouver(grille.getIdGrille());

        existing.setPoste(grille.getPoste());
        existing.setDepartement(grille.getDepartement());
        existing.setSalaireMin(grille.getSalaireMin());
        existing.setSalaireMax(grille.getSalaireMax());
        existing.setSalaireMoyen(grille.getSalaireMoyen());

        return grilleRepository.save(existing);
    }

    private void validerGrille(GrilleSalarial grille) {
        Map<String, String> errors = new LinkedHashMap<>();

        // Poste
        if (grille.getPoste() == null || grille.getPoste().getIdPoste() == null) {
            errors.put("poste.idPoste", "Le poste est obligatoire.");
        }

        // Salaires (Null et Négatif)
        if (grille.getSalaireMin() == null) {
            errors.put("salaireMin", "Le salaire minimum est obligatoire.");
        } else if (grille.getSalaireMin().signum() < 0) {
            errors.put("salaireMin", "Le salaire minimum doit être positif ou nul.");
        }

        if (grille.getSalaireMax() == null) {
            errors.put("salaireMax", "Le salaire maximum est obligatoire.");
        } else if (grille.getSalaireMax().signum() < 0) {
            errors.put("salaireMax", "Le salaire maximum doit être positif ou nul.");
        }

        if (grille.getSalaireMoyen() == null) {
            errors.put("salaireMoyen", "Le salaire moyen est obligatoire.");
        } else if (grille.getSalaireMoyen().signum() < 0) {
            errors.put("salaireMoyen", "Le salaire moyen doit être positif ou nul.");
        }

        // Cohérence des montants
        if (grille.getSalaireMin() != null && grille.getSalaireMax() != null 
                && grille.getSalaireMax().compareTo(grille.getSalaireMin()) < 0) {
            errors.put("salaireMax", "Le salaire maximum doit être supérieur ou égal au minimum.");
        }

        if (grille.getSalaireMin() != null && grille.getSalaireMax() != null && grille.getSalaireMoyen() != null) {
            if (grille.getSalaireMoyen().compareTo(grille.getSalaireMin()) < 0 
                    || grille.getSalaireMoyen().compareTo(grille.getSalaireMax()) > 0) {
                errors.put("salaireMoyen", "Le salaire moyen doit être compris entre le minimum et le maximum.");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }
    }

    @Transactional(readOnly = true)
    public Optional<GrilleSalarial> getGrillePourPosteEtDepartement(Integer idPoste, Integer idDepartement) {
        return grilleRepository.findFirstByPosteIdPosteAndDepartementIdDepartement(idPoste, idDepartement);
    }

    @Transactional(readOnly = true)
    public Optional<GrilleSalarial> getGrillePourPoste(Integer idPoste) {
        return grilleRepository.findFirstByPosteIdPoste(idPoste);
    }

    @Transactional(readOnly = true)
    public Page<GrilleSalarial> getGrillesFiltrees(GrilleSalarialFilter filter, Pageable pageable) {
        return grilleRepository.findWithFilters(filter, pageable);
    }
}