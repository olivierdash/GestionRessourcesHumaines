package com.example.sirh.service;

import com.example.sirh.model.DesactivationAcces;
import com.example.sirh.model.RuptureContrat;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DesactivationAccesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * @author moi 
 * module 12
 */

@Service
@RequiredArgsConstructor
@Transactional
public class DesactivationAccesService {

    private final DesactivationAccesRepository repo;

    /** RÈGLE : checklist standard = 5 types d'accès. Créée à l'entrée en OFFBOARDING. */
    public void initChecklist(RuptureContrat rupture) {
        if (repo.countByRuptureIdRupture(rupture.getIdRupture()) > 0) return; // idempotent
        for (String type : List.of("BADGE", "COMPTE_APPLICATIF", "VPN", "EMAIL", "TELEPHONE")) {
            repo.save(DesactivationAcces.builder()
                    .rupture(rupture)
                    .typeAcces(type)
                    .statut("A_FAIRE")
                    .build());
        }
    }

    /** RÈGLE : passer en FAIT exige une date de désactivation. */
    public DesactivationAcces marquerFait(Integer id, Utilisateur effectuePar, String commentaire) {
        DesactivationAcces d = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ligne introuvable"));
        if ("FAIT".equals(d.getStatut()))
            throw new IllegalStateException("Déjà marqué FAIT");

        d.setStatut("FAIT");
        d.setDateDesactivation(LocalDate.now());
        d.setEffectuePar(effectuePar);
        d.setCommentaire(commentaire);
        return repo.save(d);
    }

    /** Bloque la clôture tant que tout n'est pas FAIT. */
    public boolean toutEstDesactive(Integer idRupture) {
        long total = repo.countByRuptureIdRupture(idRupture);
        long restants = repo.countByRuptureIdRuptureAndStatutNot(idRupture, "FAIT");
        return total > 0 && restants == 0;
    }

    public List<DesactivationAcces> lister(Integer idRupture) {
        return repo.findByRuptureIdRupture(idRupture);
    }
}