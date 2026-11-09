package com.example.sirh.service;

import com.example.sirh.model.Affectation;
import com.example.sirh.model.Candidature;
import com.example.sirh.model.Contrat;
import com.example.sirh.model.Employe;
import com.example.sirh.model.Poste;
import com.example.sirh.model.PropositionEmbauche;
import com.example.sirh.model.TypeContrat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static com.example.sirh.constants.ConstantSirh.Statut;

@Service
@RequiredArgsConstructor
public class EmbaucheService {

    private final PropositionService propositionService;
    private final EmployeService employeService;
    private final ContratService contratService;
    private final AffectationService affectationService;
    private final StatutEmployeService statutEmployeService;
    private final OnboardingService onboardingService;

    @Transactional
    public Employe creerDossier(Integer idProposition, LocalDate dateDebut, LocalDate dateFin) {
        PropositionEmbauche p = propositionService.getProposition(idProposition);

        if (!Statut.ACCEPTE.equals(p.getStatut().getLibelle())) {
            throw new IllegalStateException(
                    "Le dossier ne peut être créé qu'après acceptation du candidat.");
        }

        Candidature candidature = p.getCandidature();

        if (employeService.existsByCandidature(candidature.getIdCandidature())) {
            throw new IllegalStateException(
                    "Le dossier salarié existe déjà pour cette candidature.");
        }

        TypeContrat type = candidature.getDemandeRecrutement().getTypeContrat();
        if (type == null) {
            throw new IllegalStateException(
                    "Le type de contrat de la demande est manquant. Corrigez la demande avant de créer le dossier.");
        }

        if (dateDebut == null) {
            throw new IllegalArgumentException("La date de début est obligatoire.");
        }
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException(
                    "La date de fin ne peut pas précéder la date de début.");
        }

        Poste poste = candidature.getDemandeRecrutement().getPoste();
        BigDecimal salaire = p.getSalaireFinal() != null
                ? p.getSalaireFinal()
                : p.getSalairePropose();

        Employe employe = employeService.save(Employe.builder()
                .candidature(candidature)
                .hiringDate(dateDebut)
                .statusEmploye(statutEmployeService.getByLibelle(Statut.EN_ONBOARDING))
                .build());

        contratService.creer(Contrat.builder()
                .employe(employe)
                .poste(poste)
                .typeContrat(type)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .salaireMensuel(salaire)
                .build());

        affectationService.save(Affectation.builder()
                .employe(employe)
                .poste(poste)
                .dateDebut(dateDebut)
                .build());

        onboardingService.creer(employe);

        return employe;
    }

    @Transactional(readOnly = true)
    public Optional<Employe> trouverEmploye(Integer idCandidature) {
        return employeService.findByCandidature(idCandidature);
    }

    @Transactional(readOnly = true)
    public Employe getEmploye(Integer idEmployer) {
        return employeService.getById(idEmployer);
    }
}