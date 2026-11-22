package com.example.sirh.service;

import com.example.sirh.model.ReponseEtDecision;
import com.example.sirh.repository.ReponseEtDecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

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
        if (d.getDateReponse() == null) d.setDateReponse(LocalDate.now());

        boolean sanction = d.getTypeSanction() != null && !"AUCUNE".equals(d.getTypeSanction());
        if (sanction && d.getDateNotification() == null)
            throw new IllegalArgumentException("Sanction sans date_notification");

        Integer idIncident = d.getDemande().getIncident().getIdIncident();
        ReponseEtDecision decision = repo.findByDemandeIdDemande(d.getDemande().getIdDemande())
                .orElseGet(() -> d);
        if (decision != d && !"REPONSE_RECUE".equals(
                d.getDemande().getIncident().getStatutInstruction())) {
            throw new IllegalStateException("La décision pour cette demande est déjà clôturée");
        }
        decision.setDemande(d.getDemande());
        decision.setDateReponse(d.getDateReponse());
        decision.setContenuReponse(d.getContenuReponse());
        decision.setTypeSanction(d.getTypeSanction() == null ? "AUCUNE" : d.getTypeSanction());
        decision.setDateNotification(d.getDateNotification());
        decision.setCommentaireDecision(d.getCommentaireDecision());
        ReponseEtDecision saved = repo.save(decision);

        if (sanction) {
            if ("EXPLICATION_EN_COURS".equals(d.getDemande().getIncident().getStatutInstruction())) {
                incidentService.changerStatut(idIncident, "REPONSE_RECUE");
            }
            incidentService.changerStatut(idIncident, "DECIDE");
        } else {
            if ("EXPLICATION_EN_COURS".equals(d.getDemande().getIncident().getStatutInstruction())) {
                incidentService.changerStatut(idIncident, "REPONSE_RECUE");
            }
            incidentService.changerStatut(idIncident, "CLOTURE_SANS_SUITE");
        }
        return saved;
    }

    public ReponseEtDecision enregistrerReponse(DemandeExplication demande, String contenu) {
        if (repo.findByDemandeIdDemande(demande.getIdDemande()).isPresent()) {
            throw new IllegalStateException("Une réponse existe déjà pour cette demande");
        }
        if (!"EXPLICATION_EN_COURS".equals(demande.getIncident().getStatutInstruction())) {
            throw new IllegalStateException("La demande n'accepte plus de réponse");
        }

        ReponseEtDecision reponse = ReponseEtDecision.builder()
                .demande(demande)
                .dateReponse(LocalDate.now())
                .contenuReponse(contenu)
                .typeSanction("AUCUNE")
                .build();
        ReponseEtDecision saved = repo.save(reponse);
        incidentService.changerStatut(demande.getIncident().getIdIncident(), "REPONSE_RECUE");
        return saved;
    }

    public Optional<ReponseEtDecision> findByDemande(Integer idDemande) {
        return repo.findByDemandeIdDemande(idDemande);
    }
}