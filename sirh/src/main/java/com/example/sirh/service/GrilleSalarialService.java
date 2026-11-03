package com.example.sirh.service;

import com.example.sirh.model.Departement;
import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.model.Poste;
import com.example.sirh.repository.GrilleSalarialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class GrilleSalarialService {

    private final GrilleSalarialRepository grilleRepository;
    private final PosteService posteService;
    private final DepartementService departementService;

    @Transactional(readOnly = true)
    public List<GrilleSalarial> lister() {
        return grilleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public GrilleSalarial trouver(Integer id) {
        return grilleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Grille salariale introuvable : " + id));
    }

    public GrilleSalarial creer(
            Integer idPoste,
            Integer idDepartement,
            BigDecimal salaireMin,
            BigDecimal salaireMax,
            BigDecimal salaireMoyen) {

        verifierMontants(salaireMin, salaireMax, salaireMoyen);

        if (grilleRepository.existsByPosteIdPosteAndDepartementIdDepartement(
                idPoste, idDepartement)) {
            throw new IllegalArgumentException(
                    "Une grille existe déjà pour ce poste et ce département.");
        }

        Poste poste = posteService.getById(idPoste);
        Departement departement = departementService.getById(idDepartement);

        GrilleSalarial grille = GrilleSalarial.builder()
                .poste(poste)
                .departement(departement)
                .salaireMin(salaireMin)
                .salaireMax(salaireMax)
                .salaireMoyen(salaireMoyen)
                .build();

        return grilleRepository.save(grille);
    }

    public GrilleSalarial modifier(
            Integer id,
            Integer idPoste,
            Integer idDepartement,
            BigDecimal salaireMin,
            BigDecimal salaireMax,
            BigDecimal salaireMoyen) {

        verifierMontants(salaireMin, salaireMax, salaireMoyen);

        if (grilleRepository.existsByPosteIdPosteAndDepartementIdDepartementAndIdGrilleNot(
                idPoste, idDepartement, id)) {
            throw new IllegalArgumentException(
                    "Une autre grille existe déjà pour ce poste et ce département.");
        }

        GrilleSalarial grille = trouver(id);
        Poste poste = posteService.getById(idPoste);
        Departement departement = departementService.getById(idDepartement);

        grille.setPoste(poste);
        grille.setDepartement(departement);
        grille.setSalaireMin(salaireMin);
        grille.setSalaireMax(salaireMax);
        grille.setSalaireMoyen(salaireMoyen);

        return grilleRepository.save(grille);
    }

    private void verifierMontants(
            BigDecimal salaireMin,
            BigDecimal salaireMax,
            BigDecimal salaireMoyen) {

        if (salaireMin == null || salaireMax == null || salaireMoyen == null) {
            throw new IllegalArgumentException("Tous les montants sont obligatoires.");
        }
        if (salaireMin.signum() < 0 || salaireMax.signum() < 0 || salaireMoyen.signum() < 0) {
            throw new IllegalArgumentException("Les salaires ne peuvent pas être négatifs.");
        }
        if (salaireMax.compareTo(salaireMin) < 0) {
            throw new IllegalArgumentException(
                    "Le salaire maximum doit être supérieur au minimum.");
        }
        if (salaireMoyen.compareTo(salaireMin) < 0 || salaireMoyen.compareTo(salaireMax) > 0) {
            throw new IllegalArgumentException(
                    "Le salaire moyen doit être compris dans la fourchette.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<GrilleSalarial> getGrillePourPosteEtDepartement(
            Integer idPoste, Integer idDepartement) {
        return grilleRepository.findFirstByPosteIdPosteAndDepartementIdDepartement(
                idPoste, idDepartement);
    }

    /**
     * Utilisé par ContratService.verifierSalaire — la grille par poste seul
     * (sans département) reste la référence légale historique.
     */
    @Transactional(readOnly = true)
    public Optional<GrilleSalarial> getGrillePourPoste(Integer idPoste) {
        return grilleRepository.findFirstByPosteIdPoste(idPoste);
    }
}