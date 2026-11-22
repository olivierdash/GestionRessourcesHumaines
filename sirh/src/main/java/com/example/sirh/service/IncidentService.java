package com.example.sirh.service;
import com.example.sirh.model.Incident;
import com.example.sirh.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author moi
 * module 11
 * IncidentService
 */
@Service 
@RequiredArgsConstructor 
@Transactional
public class IncidentService {
    private final IncidentRepository repo;

    /** RÈGLE : un incident démarre toujours en OUVERT. */
    public Incident declarer(Incident incident) {
        if (incident.getStatutInstruction() == null)
            incident.setStatutInstruction("OUVERT");
        return repo.save(incident);
    }

    public List<Incident> findByEmploye(Integer idEmployer) {
        return repo.findByEmployeIdEmployer(idEmployer);
    }

    public List<Incident> findByStatut(String statut) {
        return repo.findByStatutInstruction(statut);
    }

    public Incident findById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Incident introuvable: " + id));
    }

    /** RÈGLE : seules les transitions vers un statut supérieur sont autorisées. */
    public Incident changerStatut(Integer id, String nouveau) {
        Incident inc = findById(id);
        if (!estTransitionValide(inc.getStatutInstruction(), nouveau))
            throw new IllegalStateException(
                "Transition interdite : " + inc.getStatutInstruction() + " → " + nouveau);
        inc.setStatutInstruction(nouveau);
        return repo.save(inc);
    }

    private boolean estTransitionValide(String actuel, String nouveau) {
        // Hiérarchie simple : OUVERT → EXPLICATION_EN_COURS → REPONSE_RECUE → DECIDE
        // N'importe quel état peut aller vers CLOTURE_SANS_SUITE.
        if ("CLOTURE_SANS_SUITE".equals(nouveau)) return !"DECIDE".equals(actuel);
        return switch (actuel) {
            case "OUVERT"               -> "EXPLICATION_EN_COURS".equals(nouveau);
            case "EXPLICATION_EN_COURS" -> "REPONSE_RECUE".equals(nouveau);
            case "REPONSE_RECUE"        -> "DECIDE".equals(nouveau);
            default -> false;
        };
    }


}
