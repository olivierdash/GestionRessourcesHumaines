package com.example.sirh.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.dto.DroitCongeForm;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.DroitConge;
import com.example.sirh.model.TypeConge;
import com.example.sirh.model.TypeContrat;
import com.example.sirh.model.enums.CategorieTypeConge;
import com.example.sirh.model.enums.ModeAcquisitionDroit;
import com.example.sirh.repository.DroitCongeRepository;
import com.example.sirh.repository.TypeContractRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DroitCongeService {

    private final DroitCongeRepository droitCongeRepository;
    private final TypeCongeService typeCongeService;
    private final TypeContractRepository typeContratRepository;

    @Transactional(readOnly = true)
    public List<DroitConge> lister() {
        return droitCongeRepository.findAllByOrderByTypeCongeLibelleAscAncienneteMinMoisAsc();
    }

    @Transactional(readOnly = true)
    public List<DroitConge> listerActifs() {
        return droitCongeRepository.findByActifTrueOrderByTypeCongeLibelleAscAncienneteMinMoisAsc();
    }

    @Transactional(readOnly = true)
    public List<DroitConge> listerParType(Integer typeId) {
        return droitCongeRepository.findByTypeCongeIdAndActifTrue(typeId);
    }

    @Transactional(readOnly = true)
    public DroitConge getById(Integer id) {
        return droitCongeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Règle de droit introuvable : " + id));
    }

    @Transactional
    public DroitConge enregistrer(DroitCongeForm form) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (form.getIdTypeConge() == null) {
            errors.put("typeConge", "Le type de congé est obligatoire.");
        }
        if (form.getJoursParAn() == null || form.getJoursParAn().signum() <= 0) {
            errors.put("joursParAn", "Le nombre de jours par an doit être positif.");
        }
        if (form.getAncienneteMinMois() == null || form.getAncienneteMinMois() < 0) {
            errors.put("ancienneteMinMois", "L'ancienneté minimale doit être positive ou nulle.");
        }
        if (form.getAncienneteMaxMois() != null
                && form.getAncienneteMinMois() != null
                && form.getAncienneteMaxMois() < form.getAncienneteMinMois()) {
            errors.put("ancienneteMaxMois",
                    "L'ancienneté maximale doit être supérieure ou égale à l'ancienneté minimale.");
        }

        TypeConge type = null;
        if (form.getIdTypeConge() != null) {
            type = typeCongeService.getById(form.getIdTypeConge());
            if (type.getCategorie() != CategorieTypeConge.CONGE
                    || !Boolean.TRUE.equals(type.getConsommeSolde())) {
                errors.put("typeConge",
                        "Une règle de droit ne peut être définie que pour un congé consommant un solde.");
            }
        }

        TypeContrat contrat = null;
        if (form.getIdTypeContrat() != null) {
            contrat = typeContratRepository.findById(form.getIdTypeContrat())
                    .orElseThrow(() -> new IllegalArgumentException("Type de contrat introuvable."));
        }

        ModeAcquisitionDroit mode;
        try {
            mode = form.getModeAcquisition() == null
                    ? ModeAcquisitionDroit.ANNUEL
                    : ModeAcquisitionDroit.valueOf(form.getModeAcquisition());
        } catch (IllegalArgumentException e) {
            errors.put("modeAcquisition", "Mode d'acquisition invalide.");
            mode = ModeAcquisitionDroit.ANNUEL;
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        DroitConge cible = form.getId() == null ? new DroitConge() : getById(form.getId());
        cible.setTypeConge(type);
        cible.setTypeContrat(contrat);
        cible.setAncienneteMinMois(form.getAncienneteMinMois());
        cible.setAncienneteMaxMois(form.getAncienneteMaxMois());
        cible.setModeAcquisition(mode);
        cible.setActif(form.getActif() == null || form.getActif());
        cible.setDescription(form.getDescription());

        verifierChevauchement(cible);
        return droitCongeRepository.save(cible);
    }

    @Transactional
    public void basculerActif(Integer id) {
        DroitConge droit = getById(id);
        droit.setActif(!Boolean.TRUE.equals(droit.getActif()));
        droitCongeRepository.save(droit);
    }

    @Transactional(readOnly = true)
    public Optional<DroitConge> trouverRegle(TypeConge type, TypeContrat contrat, int ancienneteMois) {
        Integer contratId = contrat == null ? null : contrat.getId();

        return droitCongeRepository.findByTypeCongeIdAndActifTrue(type.getId()).stream()
                .filter(d -> d.getTypeContrat() == null
                        || Objects.equals(d.getTypeContrat().getId(), contratId))
                .filter(d -> ancienneteMois >= valeur(d.getAncienneteMinMois()))
                .filter(d -> d.getAncienneteMaxMois() == null
                        || ancienneteMois <= d.getAncienneteMaxMois())
                .sorted(Comparator
                        .comparing((DroitConge d) -> d.getTypeContrat() != null ? 0 : 1)
                        .thenComparing(d -> valeur(d.getAncienneteMinMois()), Comparator.reverseOrder()))
                .findFirst();
    }

    private void verifierChevauchement(DroitConge candidat) {
        if (candidat.getTypeConge() == null || candidat.getTypeConge().getId() == null) {
            return;
        }
        Integer contratId = candidat.getTypeContrat() == null
                ? null : candidat.getTypeContrat().getId();

        for (DroitConge existant : droitCongeRepository
                .findByTypeCongeIdAndActifTrue(candidat.getTypeConge().getId())) {
            if (candidat.getId() != null && candidat.getId().equals(existant.getId())) {
                continue;
            }
            if (!Boolean.TRUE.equals(existant.getActif())) {
                continue;
            }
            Integer existantContratId = existant.getTypeContrat() == null
                    ? null : existant.getTypeContrat().getId();
            if (!Objects.equals(contratId, existantContratId)) {
                continue;
            }
            if (chevauche(candidat, existant)) {
                throw new ValidationMetierException(Map.of("ancienneteMinMois",
                        "Une règle active couvre déjà cette ancienneté pour ce type de congé "
                                + "et ce contrat."));
            }
        }
    }

    private boolean chevauche(DroitConge a, DroitConge b) {
        int aMin = valeur(a.getAncienneteMinMois());
        int bMin = valeur(b.getAncienneteMinMois());
        Integer aMax = a.getAncienneteMaxMois();
        Integer bMax = b.getAncienneteMaxMois();
        boolean aAvantB = bMax != null && aMin > bMax;
        boolean bAvantA = aMax != null && bMin > aMax;
        return !aAvantB && !bAvantA;
    }

    private int valeur(Integer valeur) {
        return valeur == null ? 0 : valeur;
    }
}
