package com.example.sirh.service;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void definirUtilisateurCourant(Utilisateur utilisateur) {
        entityManager.createNativeQuery("SELECT set_config('app.user_id', :userId, true)")
                .setParameter("userId", utilisateur.getIdUtilisateur().toString())
                .getSingleResult();
    }

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

    @Transactional
    public void enregistrerConsultationSensible(Utilisateur utilisateur, String entiteCible,
                                                 Integer idEntiteCible, String details) {
        enregistrerActionSensible(utilisateur, "CONSULTATION", entiteCible, idEntiteCible, details);
    }

    @Transactional
    public void enregistrerActionSensible(Utilisateur utilisateur, String action, String entiteCible,
                                           Integer idEntiteCible, String details) {
        journalAuditRepo.save(com.example.sirh.model.JournalAudit.builder()
                .utilisateur(utilisateur)
                .action(action)
                .entiteCible(entiteCible)
                .idEntiteCible(idEntiteCible)
                .details(details)
                .sensible(true)
                .build());
    }
}
