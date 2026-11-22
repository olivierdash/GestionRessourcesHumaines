package com.example.sirh.service;

import com.example.sirh.model.ReponseEtDecision;
import com.example.sirh.repository.ReponseEtDecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * @author moi
 * module 11
 */

@Service
@RequiredArgsConstructor
@Transactional
public class ReponseEtDecisionService {

    private final ReponseEtDecisionRepository repo;
    private final IncidentService incidentService;

    /** RÈGLES :
     *  - une seule décision par demande (UNIQUE SQL)
     *  - si sanction != AUCUNE → date_notification obligatoire
     *  - statut incident : REPONSE_RECUE si AUCUNE, DECIDE sinon
     */
    public ReponseEtDecision enregistrer(ReponseEtDecision d) {
        Integer idIncident = d.getDemande().getIncident().getIdIncident();

        if (repo.findByDemandeIdDemande(d.getDemande().getIdDemande()).isPresent())
            throw new IllegalStateException("Décision déjà enregistrée pour cette demande");

        if (d.getDateReponse() == null) d.setDateReponse(LocalDate.now());

        boolean sanction = d.getTypeSanction() != null && !"AUCUNE".equals(d.getTypeSanction());
        if (sanction && d.getDateNotification() == null)
            throw new IllegalArgumentException("Sanction sans date_notification");

        ReponseEtDecision saved = repo.save(d);

        if (sanction) {
            incidentService.changerStatut(idIncident, "DECIDE");
        } else {
            incidentService.changerStatut(idIncident, "REPONSE_RECUE");
            incidentService.changerStatut(idIncident, "CLOTURE_SANS_SUITE");
        }
        return saved;
    }
}