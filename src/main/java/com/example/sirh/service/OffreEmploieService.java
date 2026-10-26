package com.example.sirh.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.model.OffreEmploi;
import com.example.sirh.model.StatutOffreEmploi;
import com.example.sirh.repository.DemandeRecrutementRepository;
import com.example.sirh.repository.OffreEmploieRepository;
import com.example.sirh.repository.StatutOffreEmploieRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * @author vox
 * OffreEmploieService
 */
@Service 
@AllArgsConstructor 
public class OffreEmploieService {
    private OffreEmploieRepository offreEmploieRepo;
    private StatutOffreEmploieRepository  statutOffreRepo;
    private DemandeRecrutementRepository demandeRecrutementRepo;

    /**
     * task 8 : lister tout les offres d'emploie
     */
    public List<OffreEmploi> listAllEmploie(){
        return offreEmploieRepo.findAllByOrderByDatePublicationDesc();
    }
    
    /**
     * task 9 : créer/publié tout les offres d'emploie
     */ 

    @Transactional 
    public OffreEmploi createNewOffer(Integer id_demande_recrutement,
            String title,
            String description,
            Integer id_statut_offre_emploie){
        DemandeRecrutement demandeRecrutement = demandeRecrutementRepo.findById(id_demande_recrutement)
            .orElseThrow(() -> new RuntimeException("la demande de recrutement correspondant à l'id"+id_demande_recrutement+" est introuvable"));

        StatutOffreEmploi statutOffreEmploi = statutOffreRepo.findById(id_statut_offre_emploie)
            .orElseThrow(() -> new RuntimeException("Statut BROUILLON introuvable"));

        OffreEmploi newoffer = OffreEmploi.builder()
            .demandeRecrutement(demandeRecrutement)
            .titre(title)
            .description(description)
            .statut(statutOffreEmploi)
            .build();

        return offreEmploieRepo.save(newoffer);
    }

    /**
     * task 9 bis: publié une offre
     */
    @Transactional 
    public void publicationOffre(Integer id_offre){
        OffreEmploi offer = offreEmploieRepo.findById(id_offre)
        .orElseThrow(() -> new RuntimeException("Offre introuvable"));
        StatutOffreEmploi statut_publication = statutOffreRepo.findById(2)
            .orElseThrow(() -> new RuntimeException("Statut PUBLIEE introuvable"));
        offer.setStatut(statut_publication);
        offer.setDatePublication(LocalDateTime.now());
        offreEmploieRepo.save(offer);
    }

    /**
     * correction d'une offre
     */
    public OffreEmploi getOffreEmploiById(Integer id_offre){
        OffreEmploi offer = offreEmploieRepo.findById(id_offre)
        .orElseThrow(() -> new RuntimeException("Offre introuvable"));
        return offer;
    }
    @Transactional 
    public void updateoffer(Integer idOffre, String nouveauTitre, String nouvelleDescription){
        OffreEmploi offre = getOffreEmploiById(idOffre);
        if(offre.getStatut().getId().equals(3)){
            throw new RuntimeException("Impossible de modifier une offre déjà clôturée.");
        }
        offre.setTitre(nouveauTitre);
        offre.setDescription(nouvelleDescription);
        offreEmploieRepo.save(offre);
    }

    /**
     * expiration après date_cloture de l'offre
     */
    @Transactional 
    public void expireoffer(Integer id_offre){
        OffreEmploi offer = this.getOffreEmploiById(id_offre);
        if (offer.getDateCloture() != null && LocalDateTime.now().isAfter(offer.getDateCloture())) {
            StatutOffreEmploi statutCloturee = statutOffreRepo.findById(3).orElseThrow();
            offer.setStatut(statutCloturee);
            offreEmploieRepo.save(offer);
        }
    }
}
