package com.example.sirh.service;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sirh.dto.DemandeRecrutementDTO;
import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.model.EtatDemande;
import com.example.sirh.model.HistoriqueDemandeRecrutement;
import com.example.sirh.model.MotifDemande;
import com.example.sirh.model.Poste;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DemandeRecrutementRepository;
import com.example.sirh.repository.HistoriqueDemandeRecrutementRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DemandeRecrutementService {

    private UtilisateurService utilisateurService;
    private PosteService posteService;
    private MotifDemandeService motifDemandeService;
    private DemandeRecrutementRepository demandeRepository;
    private EtatDemandeService etatDemandeService;
    private HistoriqueDemandeRecrutementRepository historiqueDemandeRecrutementRepository;

    @Transactional
    public DemandeRecrutement createDemande(DemandeRecrutementDTO dto) {
        Utilisateur manager = utilisateurService.getById(dto.getIdManager());
        Poste poste = posteService.getById(dto.getIdPoste());
        MotifDemande motif = motifDemandeService.getById(dto.getMotif().getIdMotifDemande());
        EtatDemande statutBrouillon = etatDemandeService.getByIntitule("Brouillon");

        DemandeRecrutement demande = DemandeRecrutement.builder()
                .manager(manager)
                .poste(poste)
                .dateSouhaitee(dto.getDateSouhaitee())
                .effectif(dto.getEffectif())
                .motif(motif)
                .etatDemande(statutBrouillon)
                .commentaires(dto.getRemarque())
                .build();

        return demandeRepository.save(demande);
    }

    public DemandeRecrutement getById(Integer id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Demande introuvable"));
    }

    @Transactional
    public DemandeRecrutement updateDemande(DemandeRecrutementDTO dto, Utilisateur validateur, String action) {
        DemandeRecrutement demande = getById(dto.getIdDemandeRecrutement());
        EtatDemande etatActuel = demande.getEtatDemande();
        String etatIntitule = etatActuel.getIntitule().toLowerCase().trim();
        String role = validateur.getRole().getLibelle().toLowerCase().trim();

        // Vérification de sécurité
        if (!"brouillon".equals(etatIntitule) || !"manager".equals(role)) {
            throw new RuntimeException("Modification Impossible : seul le manager peut modifier un brouillon.");
        }

        // Récupération des entités liées
        Utilisateur manager = utilisateurService.getById(dto.getIdManager());
        Poste poste = posteService.getById(dto.getIdPoste());
        MotifDemande motif = motifDemandeService.getById(dto.getMotif().getIdMotifDemande());

        // Mise à jour des champs
        demande.setManager(manager);
        demande.setPoste(poste);
        demande.setDateSouhaitee(dto.getDateSouhaitee());
        demande.setEffectif(dto.getEffectif());
        demande.setMotif(motif);
        demande.setCommentaires(dto.getRemarque());

        if ("submit".equals(action)) {
            EtatDemande nouvelEtat = etatDemandeService.getByIntitule("Cree");
            demande.setEtatDemande(nouvelEtat);

            demandeRepository.save(demande);
            saveHistorique(demande, validateur);
        } else {
            demandeRepository.save(demande);
        }

        return demande;
    }

    @Transactional
    public DemandeRecrutement updateStateDemande(Integer idDemande, Utilisateur validateur, String remarque,
            String action) {
        DemandeRecrutement demande = getById(idDemande);
        demande.setCommentaires(remarque);

        EtatDemande etatActuel = demande.getEtatDemande();
        String etatIntitule = etatActuel.getIntitule().toLowerCase().trim();
        String role = validateur.getRole().getLibelle().toLowerCase().trim();

        EtatDemande nouvelEtat = null;

        if ("refuser".equals(action)) {
            if ("rh".equals(role) || "finance".equals(role) || "dg".equals(role)) {
                nouvelEtat = etatDemandeService.getByIntitule("Refuse");
            } else {
                throw new RuntimeException("Seul un validateur peut refuser une demande.");
            }
        }
        else {
            if ("cree".equalsIgnoreCase(etatIntitule) && "rh".equals(role)) {
                nouvelEtat = etatDemandeService.getByIntitule("ValideRH");
            } else if ("validerh".equalsIgnoreCase(etatIntitule) && "finance".equals(role)) {
                nouvelEtat = etatDemandeService.getByIntitule("ValideFinance");
            } else if ("validefinance".equalsIgnoreCase(etatIntitule) && "dg".equals(role)) {
                nouvelEtat = etatDemandeService.getByIntitule("Valide");
            }
        }

        if (nouvelEtat == null) {
            throw new RuntimeException("Action impossible pour cet état ou ce rôle.");
        }

        demande.setEtatDemande(nouvelEtat);
        demandeRepository.save(demande);
        saveHistorique(demande, validateur);

        return demande;
    }

    @Transactional
    public void saveHistorique(DemandeRecrutement demande, Utilisateur validateur) {
        HistoriqueDemandeRecrutement histo = new HistoriqueDemandeRecrutement();
        histo.setCommentaires(demande.getCommentaires());
        histo.setDemandeRecrutement(demande);
        histo.setEtatDemande(demande.getEtatDemande());
        histo.setValidateur(validateur);
        historiqueDemandeRecrutementRepository.save(histo);
    }
}