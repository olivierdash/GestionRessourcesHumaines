package com.example.sirh.service;

import com.example.sirh.model.IncidentDocument;
import com.example.sirh.repository.IncidentDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author moi
 * module 11 
 * IncidentDocumentService
 */

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentDocumentService {

    private final IncidentDocumentRepository repo;

    /** RÈGLE : on ne peut pas ajouter de pièce à un incident DECIDE / CLOTURE_SANS_SUITE. */
    public IncidentDocument ajouter(IncidentDocument doc) {
        String statut = doc.getIncident().getStatutInstruction();
        if ("DECIDE".equals(statut) || "CLOTURE_SANS_SUITE".equals(statut))
            throw new IllegalStateException("Incident clôturé : ajout de pièce interdit");

        if (doc.getDateDepot() == null) doc.setDateDepot(LocalDateTime.now());
        return repo.save(doc);
    }

    public List<IncidentDocument> findByIncident(Integer idIncident) {
        return repo.findByIncidentIdIncident(idIncident);
    }
}