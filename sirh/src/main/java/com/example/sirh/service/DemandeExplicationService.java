package com.example.sirh.service;

import com.example.sirh.model.DemandeExplication;
import com.example.sirh.repository.DemandeExplicationReposiroty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author moi
 * module 11
 * DemandeExplicationService
 */

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeExplicationService {

    private final DemandeExplicationReposiroty repo;
    private final IncidentService incidentService;

    /** RÈGLES :
     *  - une seule demande par incident (contrainte UNIQUE SQL)
     *  - date_limite_reponse >= date_envoi
     *  - l'incident passe automatiquement en EXPLICATION_EN_COURS
     */
    public DemandeExplication envoyer(DemandeExplication d) {
        if (d.getDateEnvoi() == null) d.setDateEnvoi(java.time.LocalDate.now());
        if (d.getDateLimiteReponse().isBefore(d.getDateEnvoi()))
            throw new IllegalArgumentException("date_limite_reponse < date_envoi");

        Integer idIncident = d.getIncident().getIdIncident();
        if (repo.findByIncidentIdIncident(idIncident).isPresent())
            throw new IllegalStateException("Une demande existe déjà pour cet incident");

        DemandeExplication saved = repo.save(d);
        incidentService.changerStatut(idIncident, "EXPLICATION_EN_COURS");
        return saved;
    }

    public DemandeExplication findByIncident(Integer idIncident) {
        return repo.findByIncidentIdIncident(idIncident).orElse(null);
    }
}