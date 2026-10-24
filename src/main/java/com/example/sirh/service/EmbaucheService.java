package com.example.sirh.service;

import com.example.sirh.model.Affectation;
import com.example.sirh.model.Candidature;
import com.example.sirh.model.Contrat;
import com.example.sirh.model.Employe;
import com.example.sirh.model.Poste;
import com.example.sirh.model.PropositionEmbauche;
import com.example.sirh.model.TypeContrat;
import com.example.sirh.repository.AffectationRepository;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.PropositionEmbaucheRepository;
import com.example.sirh.repository.TypeContractRepository;
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

    private final PropositionEmbaucheRepository propositionRepo;
    private final EmployeRepository employeRepo;
    private final ContratRepository contratRepo;
    private final AffectationRepository affectationRepo;
    private final TypeContractRepository typeContratRepo;

    /**
     * Bouton « Créer dossier » : matricule + employé + contrat + affectation en UNE transaction.
     * Si une étape échoue (exception), tout est annulé.
     */
    @Transactional
    public Employe creerDossier(Integer idProposition, Integer idTypeContrat, LocalDate dateDebut, LocalDate dateFin) {
        PropositionEmbauche p = propositionRepo.findById(idProposition)
                .orElseThrow(() -> new IllegalArgumentException("Proposition introuvable : " + idProposition));
        if (!PropositionService.ACCEPTE.equals(p.getStatut().getLibelle())) {
            throw new IllegalStateException("Le dossier ne peut être créé qu'après acceptation du candidat.");
        }
        Candidature candidature = p.getCandidature();
        if (employeRepo.existsByCandidatureIdCandidature(candidature.getIdCandidature())) {
            throw new IllegalStateException("Le dossier salarié existe déjà pour cette candidature.");
        }
        if (dateDebut == null) {
            throw new IllegalArgumentException("La date de début est obligatoire.");
        }
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas précéder la date de début.");
        }
        TypeContrat type = typeContratRepo.findById(idTypeContrat)
                .orElseThrow(() -> new IllegalArgumentException("Type de contrat introuvable : " + idTypeContrat));

        Poste poste = candidature.getDemandeRecrutement().getPoste();
        BigDecimal salaire = p.getSalaireFinal() != null ? p.getSalaireFinal() : p.getSalairePropose();

        Employe employe = employeRepo.save(Employe.builder()
                .candidature(candidature)
                .matricule(genererMatricule())
                .hiringDate(dateDebut)
                .statusEmployer("ACTIF")
                .build());

        contratRepo.save(Contrat.builder()
                .employe(employe)
                .poste(poste)
                .typeContrat(type)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .salaireMensuel(salaire)
                .status("ACTIF")
                .build());

        affectationRepo.save(Affectation.builder()
                .employe(employe)
                .poste(poste)
                .dateDebut(dateDebut)
                .build());

        return employe;
    }

    @Transactional(readOnly = true)
    public Optional<Employe> trouverEmploye(Integer idCandidature) {
        return employeRepo.findByCandidatureIdCandidature(idCandidature);
    }

    @Transactional(readOnly = true)
    public Employe getEmploye(Integer idEmployer) {
        return employeRepo.findById(idEmployer)
                .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable : " + idEmployer));
    }

    @Transactional(readOnly = true)
    public List<Contrat> contratsDe(Integer idEmployer) {
        return contratRepo.findByEmployeIdEmployer(idEmployer);
    }

    @Transactional(readOnly = true)
    public List<Affectation> affectationsDe(Integer idEmployer) {
        return affectationRepo.findByEmployeIdEmployer(idEmployer);
    }

    /** Format EMP-2026-0001. Le numéro vient d'une séquence PostgreSQL : unique et non réutilisable. */
    private String genererMatricule() {
        Long numero = employeRepo.prochainNumeroMatricule();
        return String.format("EMP-%d-%04d", LocalDate.now().getYear(), numero);
    }
}