package com.example.sirh.service;

import org.springframework.stereotype.Service;

import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.JournalAuditRepository;
import com.example.sirh.repository.UtilisateurRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class AuditService {
    private final JournalAuditRepository journalAuditRepo;
    private final UtilisateurRepository utilisateurRepo;

    /**
     * Traçabilité (côté entreprise) : enregistrement d'une quelconque consultation ou autre opération
     */
    @Transactional 
    public void enregistrerAction(String userEmail ,String action ,String entiteCible, Integer idEntiteCible, String details ){
        Utilisateur user = utilisateurRepo.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if(user != null){
            journalAuditRepo.save(
                com.example.sirh.model.JournalAudit.builder()
                    .utilisateur(user)
                    .action(action)
                    .entiteCible(entiteCible)
                    .idEntiteCible(idEntiteCible)
                    .details(details)
                    .build()
            );
        } else {
            throw new RuntimeException("Utilisateur non trouvé");
        }
    }
}
