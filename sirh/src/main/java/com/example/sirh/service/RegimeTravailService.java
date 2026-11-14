package com.example.sirh.service;

import com.example.sirh.model.HoraireTravail;
import com.example.sirh.model.RegimeTravail;
import com.example.sirh.model.TypeDecompte;
import com.example.sirh.repository.RegimeTravailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.util.List;

import static com.example.sirh.constants.ConstantSirh.TypeDecompteCode;

@Service
@RequiredArgsConstructor
public class RegimeTravailService {

    private static final BigDecimal HEURES_MAX_LEGALES = new BigDecimal("48");
    private static final BigDecimal HEURES_MAX_JOUR = new BigDecimal("12");
    private static final int JOURS_ANNUELS_MAX = 218;

    private final RegimeTravailRepository regimeRepository;
    private final TypeDecompteService typeDecompteService;

    @Transactional(readOnly = true)
    public List<RegimeTravail> getAll() {
        return regimeRepository.findAllWithRelations();
    }

    @Transactional(readOnly = true)
    public RegimeTravail getById(Integer id) {
        return regimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Régime de travail introuvable : " + id));
    }

    @Transactional
    public RegimeTravail enregistrer(RegimeTravail form) {
        TypeDecompte typeDecompte = Boolean.TRUE.equals(form.getForfaitJours())
                ? typeDecompteService.getByCode(TypeDecompteCode.JOURS)
                : typeDecompteService.getByCode(TypeDecompteCode.HEURES);

        form.setTypeDecompte(typeDecompte);
        validerRegime(form, typeDecompte);

        RegimeTravail regime = form.getId() == null ? new RegimeTravail() : getById(form.getId());

        regime.setCode(form.getCode().trim().toUpperCase());
        regime.setLibelle(form.getLibelle().trim());
        regime.setDescription(form.getDescription());
        regime.setTypeDecompte(typeDecompte);
        regime.setHeuresHebdomadaires(form.getHeuresHebdomadaires());
        //regime.setJoursHebdomadaires(form.getJoursHebdomadaires());
        regime.setJoursAnnuelsReference(form.getJoursAnnuelsReference());
        regime.setPointageObligatoire(form.getPointageObligatoire());
        regime.setSeuilHeuresSupplementaires(form.getSeuilHeuresSupplementaires());
        regime.setDureeMaxHebdomadaire(form.getDureeMaxHebdomadaire());
        regime.setDureeMaxJournaliere(form.getDureeMaxJournaliere());
        regime.setActif(form.getActif() != null ? form.getActif() : Boolean.TRUE);

        return regimeRepository.save(regime);
    }

    @Transactional
    public HoraireTravail enregistrerHoraire(HoraireTravail form) {
        if (form.getRegimeTravail() == null || form.getRegimeTravail().getId() == null) {
            throw new IllegalArgumentException("Le régime de travail est obligatoire.");
        }
        
        // Vérification de l'unité dayweek (1 à 5)
        if (form.getDayweek() == null || form.getDayweek() < 1 || form.getDayweek() > 5) {
            throw new IllegalArgumentException("Le jour de la semaine (dayweek) doit être compris entre 1 et 5.");
        }
        
        if (form.getHeureDebut() == null || form.getHeureFin() == null) {
            throw new IllegalArgumentException("Les heures de début et de fin sont obligatoires.");
        }
        if (!form.getHeureFin().isAfter(form.getHeureDebut())) {
            throw new IllegalArgumentException("L'heure de fin doit être postérieure à l'heure de début.");
        }

        int dureeBrute = (int) Duration.between(form.getHeureDebut(), form.getHeureFin()).toMinutes();
        int pause = form.getPauseMinutes() != null ? form.getPauseMinutes() : 0;
        if (pause < 0) {
            throw new IllegalArgumentException("La pause ne peut pas être négative.");
        }
        if (pause >= dureeBrute) {
            throw new IllegalArgumentException("La pause ne peut pas être supérieure ou égale à la durée de présence.");
        }

        RegimeTravail regime = getById(form.getRegimeTravail().getId());
        form.setRegimeTravail(regime);

        int minutesTravailles = dureeBrute - pause;
        if (regime.getDureeMaxJournaliere() != null
                && minutesTravailles > regime.getDureeMaxJournaliere() * 60) {
            throw new IllegalArgumentException(
                    "La durée journalière (" + minutesTravailles + " min) dépasse la durée maximale du régime ("
                            + regime.getDureeMaxJournaliere() + "h).");
        }

        // Recherche par dayweek
        HoraireTravail horaire = regime.getHoraires().stream()
                .filter(h -> h.getDayweek() != null && h.getDayweek().equals(form.getDayweek()))
                .findFirst()
                .orElseGet(() -> {
                    HoraireTravail nouveau = HoraireTravail.builder()
                            .regimeTravail(regime)
                            .dayweek(form.getDayweek())
                            .build();
                    regime.getHoraires().add(nouveau);
                    return nouveau;
                });

        horaire.setHeureDebut(form.getHeureDebut());
        horaire.setHeureFin(form.getHeureFin());
        horaire.setPauseMinutes(pause);

        regimeRepository.save(regime);
        return horaire;
    }

    /**
     * Utilisé par PointageService. Prend toujours DayOfWeek en entrée
     * et convertit en entier dayweek (1=Lundi, ..., 5=Vendredi).
     */
    @Transactional(readOnly = true)
    public HoraireTravail trouverHorairePourJour(RegimeTravail regime, DayOfWeek jour) {
        if (regime == null || regime.getHoraires() == null || jour == null) {
            return null;
        }

        // DayOfWeek.MONDAY.getValue() renvoie 1, ..., FRIDAY=5, SAMEDI=6, DIMANCHE=7
        int dayweek = jour.getValue();
        if (dayweek > 5) {
            return null; // Pas de travail le week-end
        }

        return regime.getHoraires().stream()
                .filter(h -> h.getDayweek() != null && h.getDayweek() == dayweek)
                .findFirst()
                .orElse(null);
    }

    private void validerRegime(RegimeTravail form, TypeDecompte type) {
        // ... (le reste du code de validation reste identique)
        if (form.getCode() == null || form.getCode().isBlank()) {
            throw new IllegalArgumentException("Le code est obligatoire.");
        }
        if (form.getLibelle() == null || form.getLibelle().isBlank()) {
            throw new IllegalArgumentException("Le libellé est obligatoire.");
        }

        String code = form.getCode().trim();
        String libelle = form.getLibelle().trim();

        boolean codeExiste = form.getId() == null
                ? regimeRepository.existsByCodeIgnoreCase(code)
                : regimeRepository.existsByCodeIgnoreCaseAndIdNot(code, form.getId());
        if (codeExiste) {
            throw new IllegalArgumentException("Un régime avec ce code existe déjà.");
        }

        boolean libelleExiste = form.getId() == null
                ? regimeRepository.existsByLibelleIgnoreCase(libelle)
                : regimeRepository.existsByLibelleIgnoreCaseAndIdNot(libelle, form.getId());
        if (libelleExiste) {
            throw new IllegalArgumentException("Un régime avec ce libellé existe déjà.");
        }

        if (TypeDecompteCode.HEURES.equals(type.getCode())) {
            if (form.getHeuresHebdomadaires() == null
                    || form.getHeuresHebdomadaires().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Les heures hebdomadaires sont obligatoires pour un décompte en heures.");
            }
            if (form.getHeuresHebdomadaires().compareTo(HEURES_MAX_LEGALES) > 0) {
                throw new IllegalArgumentException(
                        "Les heures hebdomadaires ne peuvent pas dépasser " + HEURES_MAX_LEGALES + "h.");
            }
            if (form.getSeuilHeuresSupplementaires() != null
                    && form.getSeuilHeuresSupplementaires().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Le seuil d'heures supplémentaires doit être positif.");
            }
            if (form.getJoursAnnuelsReference() != null) {
                throw new IllegalArgumentException(
                        "Les jours annuels de référence ne s'appliquent qu'aux forfaits jours.");
            }
        } else if (TypeDecompteCode.JOURS.equals(type.getCode())) {
            if (form.getJoursAnnuelsReference() == null || form.getJoursAnnuelsReference() <= 0) {
                throw new IllegalArgumentException(
                        "Les jours annuels de référence sont obligatoires pour un forfait jours.");
            }
            if (form.getJoursAnnuelsReference() > JOURS_ANNUELS_MAX) {
                throw new IllegalArgumentException(
                        "Le nombre de jours annuels ne peut pas dépasser " + JOURS_ANNUELS_MAX + ".");
            }
            if (form.getHeuresHebdomadaires() != null) {
                throw new IllegalArgumentException(
                        "Les heures hebdomadaires ne s'appliquent pas à un forfait jours.");
            }
        }

        if (form.getDureeMaxJournaliere() != null
                && BigDecimal.valueOf(form.getDureeMaxJournaliere()).compareTo(HEURES_MAX_JOUR) > 0) {
            throw new IllegalArgumentException(
                    "La durée maximale journalière ne peut pas dépasser " + HEURES_MAX_JOUR + "h.");
        }
        if (form.getDureeMaxHebdomadaire() != null
                && BigDecimal.valueOf(form.getDureeMaxHebdomadaire()).compareTo(HEURES_MAX_LEGALES) > 0) {
            throw new IllegalArgumentException(
                    "La durée maximale hebdomadaire ne peut pas dépasser " + HEURES_MAX_LEGALES + "h.");
        }
    }
}