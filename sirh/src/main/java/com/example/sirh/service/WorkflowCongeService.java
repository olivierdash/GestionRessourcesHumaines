package com.example.sirh.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.Employe;
import com.example.sirh.model.Utilisateur;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowCongeService {

    private final CongeAccessService accessService;

    /**
     * Manager attendu au moment de la soumission.
     * Il est snapshoté sur la demande pour ne pas être réaffecté si l'organigramme change.
     */
    @Transactional(readOnly = true)
    public Optional<Utilisateur> determinerManagerAttendu(Employe employe, Utilisateur demandeur) {
        return accessService.managerDeLEmploye(employe)
                .filter(manager -> !memeUtilisateur(manager, demandeur))
                .filter(manager -> !memeEmploye(manager, employe));
    }

    @Transactional(readOnly = true)
    public boolean managerRequis(Employe employe, Utilisateur demandeur) {
        return determinerManagerAttendu(employe, demandeur).isPresent();
    }

    @Transactional(readOnly = true)
    public void verifierValidationManager(DemandeConge demande, Utilisateur utilisateur) {
        if (!accessService.peutValiderEnTantQueManager(demande, utilisateur)) {
            Map<String, String> errors = new LinkedHashMap<>();
            errors.put("workflow",
                    "Vous n'êtes pas le manager attendu ou le contrôleur RH de cette demande.");
            throw new ValidationMetierException(errors);
        }
        if (accessService.estProprietaire(demande, utilisateur)) {
            Map<String, String> errors = new LinkedHashMap<>();
            errors.put("workflow", "Vous ne pouvez pas valider votre propre demande.");
            throw new ValidationMetierException(errors);
        }
    }

    @Transactional(readOnly = true)
    public void verifierApprobationRh(DemandeConge demande, Utilisateur utilisateur) {
        if (!accessService.peutApprouver(demande, utilisateur)) {
            Map<String, String> errors = new LinkedHashMap<>();
            errors.put("workflow",
                    "Seul un contrôleur RH ou le DG peut approuver cette demande, et jamais son demandeur.");
            throw new ValidationMetierException(errors);
        }
    }

    @Transactional(readOnly = true)
    public void verifierAnnulation(DemandeConge demande, Utilisateur utilisateur) {
        boolean autorise = accessService.estProprietaire(demande, utilisateur)
                || accessService.estRhOuDg(utilisateur);
        if (!autorise) {
            Map<String, String> errors = new LinkedHashMap<>();
            errors.put("workflow",
                    "Seul le demandeur, un contrôleur RH ou le DG peut annuler cette demande.");
            throw new ValidationMetierException(errors);
        }
    }

    private boolean memeUtilisateur(Utilisateur a, Utilisateur b) {
        return a != null && b != null
                && a.getIdUtilisateur() != null
                && a.getIdUtilisateur().equals(b.getIdUtilisateur());
    }

    private boolean memeEmploye(Utilisateur utilisateur, Employe employe) {
        return utilisateur != null
                && employe != null
                && utilisateur.getEmploye() != null
                && employe.getIdEmployer() != null
                && employe.getIdEmployer().equals(utilisateur.getEmploye().getIdEmployer());
    }
}
