package com.example.sirh.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.config.CustomUserDetails;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Absence;
import com.example.sirh.model.Affectation;
import com.example.sirh.model.Contrat;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.Departement;
import com.example.sirh.model.Employe;
import com.example.sirh.model.JustificatifConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.AffectationRepository;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

/**
 * Centralise les règles de périmètre du module congés.
 * Aucun contrôle d'accès ne doit reposer uniquement sur l'URL.
 */
@Service
@RequiredArgsConstructor
public class CongeAccessService {

    private final UtilisateurRepository utilisateurRepository;
    private final AffectationRepository affectationRepository;
    private final ContratRepository contratRepository;

    @Transactional(readOnly = true)
    public Utilisateur utilisateurCourant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new IllegalStateException("Utilisateur non authentifié.");
        }
        return utilisateurRepository
                .findById(details.getUtilisateur().getIdUtilisateur())
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable."));
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> utilisateurCourantOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            return Optional.empty();
        }
        return utilisateurRepository.findById(details.getUtilisateur().getIdUtilisateur());
    }

    @Transactional(readOnly = true)
    public Employe employeCourant() {
        Utilisateur utilisateur = utilisateurCourant();
        if (utilisateur.getEmploye() == null) {
            Map<String, String> errors = new LinkedHashMap<>();
            errors.put("employe", "Votre compte n'est lié à aucun salarié. Contactez les RH.");
            throw new ValidationMetierException(errors);
        }
        return utilisateur.getEmploye();
    }

    public boolean estRhOuDg(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getRole() == null) {
            return false;
        }
        String role = utilisateur.getRole().getLibelle();
        return "RH".equalsIgnoreCase(role) || "DG".equalsIgnoreCase(role);
    }

    public boolean estManager(Utilisateur utilisateur) {
        return utilisateur != null
                && utilisateur.getRole() != null
                && "MANAGER".equalsIgnoreCase(utilisateur.getRole().getLibelle());
    }

    public boolean estAdmin(Utilisateur utilisateur) {
        return utilisateur != null
                && utilisateur.getRole() != null
                && "ADMIN".equalsIgnoreCase(utilisateur.getRole().getLibelle());
    }

    @Transactional(readOnly = true)
    public Optional<Departement> departementDeLEmploye(Employe employe) {
        if (employe == null || employe.getIdEmployer() == null) {
            return Optional.empty();
        }
        LocalDate aujourdHui = LocalDate.now();

        List<Affectation> affectations =
                affectationRepository.findActivesForDate(employe.getIdEmployer(), aujourdHui);
        if (!affectations.isEmpty()) {
            Affectation affectation = affectations.get(0);
            if (affectation.getPoste() != null && affectation.getPoste().getDepartement() != null) {
                return Optional.of(affectation.getPoste().getDepartement());
            }
        }

        List<Contrat> contrats =
                contratRepository.findActifsPourDate(employe.getIdEmployer(), aujourdHui);
        if (!contrats.isEmpty()) {
            Contrat contrat = contrats.get(0);
            if (contrat.getPoste() != null && contrat.getPoste().getDepartement() != null) {
                return Optional.of(contrat.getPoste().getDepartement());
            }
        }

        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> managerDeLEmploye(Employe employe) {
        return departementDeLEmploye(employe)
                .map(Departement::getManager)
                .filter(manager -> manager != null && manager.getIdUtilisateur() != null);
    }

    @Transactional(readOnly = true)
    public boolean managerDuDepartement(Utilisateur utilisateur, Employe employe) {
        if (utilisateur == null || employe == null) {
            return false;
        }
        return departementDeLEmploye(employe)
                .map(Departement::getManager)
                .map(manager -> manager.getIdUtilisateur() != null
                        && manager.getIdUtilisateur().equals(utilisateur.getIdUtilisateur()))
                .orElse(false);
    }

    public boolean estProprietaire(DemandeConge demande, Utilisateur utilisateur) {
        if (demande == null || utilisateur == null || utilisateur.getEmploye() == null) {
            return false;
        }
        return demande.getEmploye() != null
                && demande.getEmploye().getIdEmployer() != null
                && demande.getEmploye().getIdEmployer()
                        .equals(utilisateur.getEmploye().getIdEmployer());
    }

    public boolean estProprietaire(Absence absence, Utilisateur utilisateur) {
        if (absence == null || utilisateur == null || utilisateur.getEmploye() == null) {
            return false;
        }
        return absence.getEmploye() != null
                && absence.getEmploye().getIdEmployer() != null
                && absence.getEmploye().getIdEmployer()
                        .equals(utilisateur.getEmploye().getIdEmployer());
    }

    public boolean estEmployeCourant(Employe employe) {
        if (employe == null || employe.getIdEmployer() == null) {
            return false;
        }
        Utilisateur utilisateur = utilisateurCourantOptional().orElse(null);
        return utilisateur != null
                && utilisateur.getEmploye() != null
                && employe.getIdEmployer().equals(utilisateur.getEmploye().getIdEmployer());
    }

    @Transactional(readOnly = true)
    public boolean peutVoirDemande(DemandeConge demande, Utilisateur utilisateur) {
        if (demande == null || utilisateur == null) {
            return false;
        }
        if (estRhOuDg(utilisateur) || estAdmin(utilisateur) || estProprietaire(demande, utilisateur)) {
            return true;
        }
        if (demande.getManagerAttendu() != null
                && demande.getManagerAttendu().getIdUtilisateur() != null
                && demande.getManagerAttendu().getIdUtilisateur()
                        .equals(utilisateur.getIdUtilisateur())) {
            return true;
        }
        return managerDuDepartement(utilisateur, demande.getEmploye());
    }

    @Transactional(readOnly = true)
    public boolean peutValiderEnTantQueManager(DemandeConge demande, Utilisateur utilisateur) {
        if (demande == null || utilisateur == null) {
            return false;
        }
        if (estProprietaire(demande, utilisateur)) {
            return false;
        }
        if (demande.getManagerAttendu() != null
                && demande.getManagerAttendu().getIdUtilisateur() != null
                && demande.getManagerAttendu().getIdUtilisateur()
                        .equals(utilisateur.getIdUtilisateur())) {
            return true;
        }
        return estRhOuDg(utilisateur) || managerDuDepartement(utilisateur, demande.getEmploye());
    }

    @Transactional(readOnly = true)
    public boolean peutApprouver(DemandeConge demande, Utilisateur utilisateur) {
        return estRhOuDg(utilisateur) && !estProprietaire(demande, utilisateur);
    }

    @Transactional(readOnly = true)
    public boolean peutGererAbsence(Absence absence, Utilisateur utilisateur) {
        if (absence == null || utilisateur == null) {
            return false;
        }
        return estRhOuDg(utilisateur)
                || estAdmin(utilisateur)
                || estProprietaire(absence, utilisateur)
                || managerDuDepartement(utilisateur, absence.getEmploye());
    }

    @Transactional(readOnly = true)
    public boolean peutVoirJustificatif(JustificatifConge justificatif, Utilisateur utilisateur) {
        if (justificatif == null || utilisateur == null) {
            return false;
        }
        if (justificatif.getDemandeConge() != null) {
            return peutVoirDemande(justificatif.getDemandeConge(), utilisateur);
        }
        return peutGererAbsence(justificatif.getAbsence(), utilisateur);
    }

    @Transactional(readOnly = true)
    public boolean peutValiderJustificatif(JustificatifConge justificatif, Utilisateur utilisateur) {
        if (justificatif == null || utilisateur == null) {
            return false;
        }
        if (estRhOuDg(utilisateur)) {
            return true;
        }
        if (estAdmin(utilisateur)) {
            return false;
        }
        Employe employe = justificatif.getDemandeConge() != null
                ? justificatif.getDemandeConge().getEmploye()
                : justificatif.getAbsence() == null ? null : justificatif.getAbsence().getEmploye();
        if (employe == null) {
            return false;
        }
        if (utilisateur.getEmploye() != null
                && employe.getIdEmployer().equals(utilisateur.getEmploye().getIdEmployer())) {
            return false;
        }
        return managerDuDepartement(utilisateur, employe);
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> managers() {
        return utilisateurRepository.findByRole_LibelleIgnoreCase("MANAGER");
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> premierRh() {
        return utilisateurRepository.findFirstByRoleLibelle("RH");
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> premierDg() {
        return utilisateurRepository.findFirstByRoleLibelle("DG");
    }
}
