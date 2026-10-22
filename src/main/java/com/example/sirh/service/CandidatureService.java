package com.example.sirh.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.sirh.model.Candidature;
import com.example.sirh.model.StatutCandidature;
import com.example.sirh.repository.CandidatRepository;
import com.example.sirh.repository.CandidatureRepository;
import com.example.sirh.repository.DemandeRecrutementRepository;
import com.example.sirh.repository.OffreEmploieRepository;
import com.example.sirh.repository.StatutCandidatureRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
/**
 * @author vox
 * CandidatureService
 */

@Service 
@AllArgsConstructor 
public class CandidatureService {
    private CandidatureRepository candidatureRepo;
    private StatutCandidatureRepository statutCandidatureRepo;

    /**
     * Tâche 10 : liste des candidatures par offre
     */
    public List<Candidature> listeCandidatureByOffre(Integer id_offre){
        return candidatureRepo.findByOffreIdOffre(id_offre);
    }

    /**
     * Tâche 11 : récupération des candidatures posées par un candidat
     */
    public List<Candidature> listeCandidatureByCandidat(Integer id_candidat){
        return candidatureRepo.findByCandidatIdCandidatOrderByDateDepotCandidatureDesc(id_candidat);
    }

    /**
     * Récupérer une candidature par son ID
     */
    public Candidature getCandidatureById(Integer id_candidature){
        return candidatureRepo.findById(id_candidature)
            .orElseThrow(() -> new RuntimeException("Candidature introuvable avec l'id " + id_candidature));
    }

    /**
     * Changer manuellement le statut d'une candidature (ex: retenu, refusé après entretien)
     */
    @Transactional 
    public void changerStatutCandidature(Integer id_candidature, Integer id_nouveau_statut) {
        Candidature candidature = this.getCandidatureById(id_candidature);
        
        StatutCandidature nouveauStatut = statutCandidatureRepo.findById(id_nouveau_statut)
            .orElseThrow(() -> new RuntimeException("Statut introuvable avec l'id " + id_nouveau_statut));
        
        candidature.setStatutCandidature(nouveauStatut);
        candidatureRepo.save(candidature);
    }
}
