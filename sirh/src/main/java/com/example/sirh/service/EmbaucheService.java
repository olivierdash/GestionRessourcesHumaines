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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmbaucheService {

    private final PropositionService propositionService;
    private final EmployeService employeService;
    private final ContratService contratService;
    private final TypeContratService typeContratService;
    private final AffectationService affectationService;
    private final StatutEmployeService statutEmployeService;

    /**
     * Bouton « Créer dossier » : matricule + employé + contrat + affectation en UNE transaction.
     * Le contrat passe par ContratService.creer(...) -> validation chevauchement + grille.
     * Si une étape échoue, tout est annulé (transaction).
     */
    @Transactional
    public Employe creerDossier(Integer idProposition, Integer idTypeContrat,
            LocalDate dateDebut, LocalDate dateFin) {

        PropositionEmbauche p = propositionService.getProposition(idProposition);

        if (!PropositionService.ACCEPTE.equals(p.getStatut().getLibelle())) {
            throw new IllegalStateException(
                    "Le dossier ne peut être créé qu'après acceptation du candidat.");
        }

        Candidature candidature = p.getCandidature();

        if (employeService.existsByCandidature(candidature.getIdCandidature())) {
            throw new IllegalStateException(
                    "Le dossier salarié existe déjà pour cette candidature.");
        }
        if (dateDebut == null) {
            throw new IllegalArgumentException("La date de début est obligatoire.");
        }
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException(
                    "La date de fin ne peut pas précéder la date de début.");
        }

        TypeContrat type = typeContratService.getById(idTypeContrat);

        Poste poste = candidature.getDemandeRecrutement().getPoste();
        BigDecimal salaire = p.getSalaireFinal() != null
                ? p.getSalaireFinal()
                : p.getSalairePropose();

        Employe employe = employeService.save(Employe.builder()
                .candidature(candidature)
                .hiringDate(dateDebut)
                .statusEmploye(statutEmployeService.getByLibelle("ACTIF")) 
                .build());

        // Délègue la validation complète (CDD, grille, chevauchement) à ContratService
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