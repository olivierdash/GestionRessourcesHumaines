package com.example.sirh.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.sirh.model.Candidature;
import com.example.sirh.model.Evaluation;
import com.example.sirh.model.StatutCandidature;
import com.example.sirh.model.TypeEvaluation;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.CandidatureRepository;
import com.example.sirh.repository.EvaluationRepository;
import com.example.sirh.repository.StatutCandidatureRepository;
import com.example.sirh.repository.TypeEvaluationRepository;
import com.example.sirh.repository.UtilisateurRepository;

import jakarta.transaction.Transactional;

@Service 
public class EvaluationService {
    
    private EvaluationRepository evaluationRepo;
    private CandidatureRepository candidatureRepo;
    private UtilisateurRepository utilisateurRepo;
    private TypeEvaluationRepository typeEvaluationRepo;
    private StatutCandidatureRepository statutCandidatureRepo;

    /**
     * Tâche 12 : Ajouter une évaluation à une candidature
     * 
     * @param idCandidature L'ID de la candidature évaluée
     * @param idEvaluateur L'ID du RH ou Manager qui fait l'évaluation
     * @param idTypeEvaluation L'ID du type (Test technique, Entretien...)
     * @param commentaire Le compte-rendu de l'évaluation
     * @param note La note sur 20
     */
    @Transactional 
    public void ajouterEvaluation(Integer idCandidature, Integer idEvaluateur, Integer idTypeEvaluation, String commentaire, Double note) {
        Candidature candidature = candidatureRepo.findById(idCandidature)
            .orElseThrow(() -> new RuntimeException("Candidature introuvable avec l'id " + idCandidature));

        Utilisateur evaluateur = utilisateurRepo.findById(idEvaluateur)
            .orElseThrow(() -> new RuntimeException("Évaluateur introuvable avec l'id " + idEvaluateur));

        TypeEvaluation typeEvaluation = typeEvaluationRepo.findById(idTypeEvaluation)
            .orElseThrow(() -> new RuntimeException("Type d'évaluation introuvable avec l'id " + idTypeEvaluation));

        Evaluation nouvelleEvaluation = Evaluation.builder()
            .candidature(candidature)
            .evaluateur(evaluateur)
            .typeEvaluation(typeEvaluation)
            .evaluation(commentaire)
            .note(note != null ? BigDecimal.valueOf(note) : null)
            .build();
        
        evaluationRepo.save(nouvelleEvaluation);

        // 5. Mise à jour du statut de la candidature
        // Si la candidature était à l'état initial "RECU" (ID 1), elle passe à "EN_EVALUATION" (ID 2)
        if (candidature.getStatutCandidature().getId().equals(1)) {
            StatutCandidature statutEnEval = statutCandidatureRepo.findById(2)
                .orElseThrow(() -> new RuntimeException("Statut EN_EVALUATION (ID 2) introuvable"));
            
            candidature.setStatutCandidature(statutEnEval);
            candidatureRepo.save(candidature);
        }
    }

    /**
     * Tâche 12 (bis) : Récupérer l'historique de toutes les évaluations d'une candidature
     */
    public List<Evaluation> listerEvaluationsParCandidature(Integer idCandidature) {
        return evaluationRepo.findByCandidatureIdCandidature(idCandidature);
    }


}
