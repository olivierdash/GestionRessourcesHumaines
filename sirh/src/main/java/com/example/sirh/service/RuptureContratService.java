package com.example.sirh.service;

import com.example.sirh.model.RuptureContrat;
import com.example.sirh.repository.RuptureContratRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author moi
 * module 12 
 */

@Service
@RequiredArgsConstructor
@Transactional
public class RuptureContratService {

    private final RuptureContratRepository repo;

    /** RÈGLES :
     *  - date_sortie >= date_notification + duree_preavis_jours
     *  - démarre toujours en INITIEE, non archivée
     */
    public RuptureContrat initier(RuptureContrat r) {
        if (r.getDateNotification() == null) r.setDateNotification(LocalDate.now());
        LocalDate sortieMini = r.getDateNotification().plusDays(r.getDureePreavisJours());
        if (r.getDateSortie().isBefore(sortieMini))
            throw new IllegalArgumentException(
                "date_sortie < date_notification + préavis (" + sortieMini + ")");

        r.setStatutRupture("INITIEE");
        r.setArchive(false);
        r.setDateCreation(LocalDateTime.now());
        return repo.save(r);
    }

    /** Transitions autorisées (une seule direction) :
     *  INITIEE → PREAVIS_EN_COURS → OFFBOARDING_EN_COURS → CLOTUREE
     *  N'importe quel état → ANNULEE (sauf déjà CLOTUREE)
     */
    public RuptureContrat changerStatut(Integer idRupture, String nouveau) {
        RuptureContrat r = findById(idRupture);

        if ("CLOTUREE".equals(r.getStatutRupture()))
            throw new IllegalStateException("Rupture déjà clôturée");
        if (!transitionValide(r.getStatutRupture(), nouveau))
            throw new IllegalStateException(
                "Transition interdite : " + r.getStatutRupture() + " → " + nouveau);

        r.setStatutRupture(nouveau);
        return repo.save(r);
    }

    private boolean transitionValide(String actuel, String nouveau) {
        if ("ANNULEE".equals(nouveau)) return true;
        return switch (actuel) {
            case "INITIEE"              -> "PREAVIS_EN_COURS".equals(nouveau);
            case "PREAVIS_EN_COURS"     -> "OFFBOARDING_EN_COURS".equals(nouveau);
            case "OFFBOARDING_EN_COURS" -> "CLOTUREE".equals(nouveau);
            default -> false;
        };
    }

    /** RÈGLE : on archive uniquement une rupture CLOTUREE. */
    public RuptureContrat archiver(Integer idRupture) {
        RuptureContrat r = findById(idRupture);
        if (!"CLOTUREE".equals(r.getStatutRupture()))
            throw new IllegalStateException("Archivage impossible : rupture non clôturée");
        r.setArchive(true);
        return repo.save(r);
    }

    public RuptureContrat findById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rupture introuvable: " + id));
    }

    public List<RuptureContrat> listerActifs() { return repo.findByArchiveFalse(); }
    public List<RuptureContrat> listerParStatut(String s) { return repo.findByStatutRupture(s); }
}