package com.example.sirh.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.sirh.model.Candidature;
import com.example.sirh.model.Evaluation;
import com.example.sirh.model.StatutCandidature;
import com.example.sirh.model.TypeEvaluation;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.EvaluationRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepo;
    private final CandidatureService candidatureService;
    private final UtilisateurService utilisateurService;
    private final TypeEvaluationService typeEvaluationService;
    private final StatutCandidatureService statutCandidatureService;

    /**
     * Tâche 12 : Ajouter une évaluation à une candidature.
     * Si la candidature est encore au statut RECU, elle passe à EN_EVALUATION.
     */
    @Transactional
    public void ajouterEvaluation(Integer idCandidature, Integer idEvaluateur,
            Integer idTypeEvaluation, String commentaire, Double note) {

        Candidature candidature = candidatureService.getCandidatureById(idCandidature);
        Utilisateur evaluateur = utilisateurService.getById(idEvaluateur);
        TypeEvaluation typeEvaluation = typeEvaluationService.getById(idTypeEvaluation);

        Evaluation nouvelleEvaluation = Evaluation.builder()
                .candidature(candidature)
                .evaluateur(evaluateur)
                .typeEvaluation(typeEvaluation)
                .evaluation(commentaire)
                .note(note != null ? BigDecimal.valueOf(note) : null)
                .build();

        evaluationRepo.save(nouvelleEvaluation);

        // Transition RECU -> EN_EVALUATION (comparaison par libellé, plus d'ID en dur)
        if ("RECU".equalsIgnoreCase(candidature.getStatutCandidature().getLibelle())) {
            StatutCandidature statutEnEval =
                    statutCandidatureService.getByLibelle("EN_EVALUATION");
            candidatureService.changerStatutCandidature(idCandidature, statutEnEval.getId());
        }
    }

    @Transactional
    public List<Evaluation> listerEvaluationsParCandidature(Integer idCandidature) {
        return evaluationRepo.findByCandidatureIdCandidature(idCandidature);
    }
}