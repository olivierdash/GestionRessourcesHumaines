package com.example.sirh.service;

import com.example.sirh.model.GrilleSalarial;
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

    /**
     * Crée une nouvelle grille. L'entité doit déjà porter un Poste et un
     * Departement managés (chargés par le controller via leurs services).
     * L'idGrille doit être null (autogénéré).
     */
    public GrilleSalarial creer(GrilleSalarial grille) {
        if (grille == null) {
            throw new IllegalArgumentException("Grille salariale requise.");
        }

        verifierMontants(grille);

        Integer idPoste = grille.getPoste().getIdPoste();
        Integer idDepartement = grille.getDepartement().getIdDepartement();

        if (grilleRepository.existsByPosteIdPosteAndDepartementIdDepartement(
                idPoste, idDepartement)) {
            throw new IllegalArgumentException(
                    "Une grille existe déjà pour ce poste et ce département.");
        }

        return grilleRepository.save(grille);
    }

    /**
     * Modifie une grille existante. L'entité passée porte l'idGrille à
     * modifier ainsi que les nouvelles valeurs (poste, departement, salaires).
     * Les entités Poste/Departement doivent être managées.
     */
    public GrilleSalarial modifier(GrilleSalarial grille) {
        if (grille == null) {
            throw new IllegalArgumentException("Grille salariale requise.");
        }

        verifierMontants(grille);

        if (grilleRepository.existsByPosteIdPosteAndDepartementIdDepartementAndIdGrilleNot(
                grille.getPoste().getIdPoste(),
                grille.getDepartement().getIdDepartement(),
                grille.getIdGrille())) {
            throw new IllegalArgumentException(
                    "Une autre grille existe déjà pour ce poste et ce département.");
        }

        GrilleSalarial existing = trouver(grille.getIdGrille());

        existing.setPoste(grille.getPoste());
        existing.setDepartement(grille.getDepartement());
        existing.setSalaireMin(grille.getSalaireMin());
        existing.setSalaireMax(grille.getSalaireMax());
        existing.setSalaireMoyen(grille.getSalaireMoyen());

        return grilleRepository.save(existing);
    }

    private void verifierMontants(GrilleSalarial grille) {
        BigDecimal salaireMin = grille.getSalaireMin();
        BigDecimal salaireMax = grille.getSalaireMax();
        BigDecimal salaireMoyen = grille.getSalaireMoyen();

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
        // if (salaireMoyen.compareTo(salaireMin) < 0 || salaireMoyen.compareTo(salaireMax) > 0) {
        //     throw new IllegalArgumentException(
        //             "Le salaire moyen doit être compris dans la fourchette.");
        // }
    }

    @Transactional(readOnly = true)
    public Optional<GrilleSalarial> getGrillePourPosteEtDepartement(
            Integer idPoste, Integer idDepartement) {
        return grilleRepository.findFirstByPosteIdPosteAndDepartementIdDepartement(
                idPoste, idDepartement);
    }

    @Transactional(readOnly = true)
    public Optional<GrilleSalarial> getGrillePourPoste(Integer idPoste) {
        return grilleRepository.findFirstByPosteIdPoste(idPoste);
    }
}