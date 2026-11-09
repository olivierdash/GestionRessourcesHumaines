package com.example.sirh.service;

import com.example.sirh.model.Contrat;
import com.example.sirh.model.Employe;
import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.model.Poste;
import com.example.sirh.model.StatutContrat;
import com.example.sirh.model.TypeContrat;
import com.example.sirh.repository.ContratRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ContratService {

    private final ContratRepository contratRepository;
    private final GrilleSalarialService grilleSalarialService;
    private final StatutContratService statutContratService;
    private final EmployeService employeService;
    private final PosteService posteService;
    private final TypeContratService typeContratService;

    public ContratService(ContratRepository contratRepository,
            GrilleSalarialService grilleSalarialService,
            StatutContratService statutContratService,
            EmployeService employeService,
            PosteService posteService,
            TypeContratService typeContratService) {
        this.contratRepository = contratRepository;
        this.grilleSalarialService = grilleSalarialService;
        this.statutContratService = statutContratService;
        this.employeService = employeService;
        this.posteService = posteService;
        this.typeContratService = typeContratService;
    }

    @Transactional(readOnly = true)
    public Contrat trouver(Integer idContrat) {
        return contratRepository.findById(idContrat)
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable : " + idContrat));
    }

    @Transactional
    public Contrat creer(Contrat contrat) {
        Integer idEmploye = contrat.getEmploye().getIdEmployer();

        verifierDureeContrat(contrat);
        verifierSalaire(contrat.getPoste().getIdPoste(), contrat.getSalaireMensuel());
        verifierPasDeChevauchement(idEmploye, contrat.getDateDebut(), contrat.getDateFin());

        StatutContrat actif = statutContratService.getByLibelle("actif");
        contrat.setStatut(actif);

        return contratRepository.save(contrat);
    }

    @Transactional
    public Contrat renouveler(Integer idAncienContrat, Contrat nouveau) {
        Contrat ancien = trouver(idAncienContrat);

        StatutContrat termine = statutContratService.getByLibelle("terminé");

        nouveau.setEmploye(ancien.getEmploye());

        ancien.setDateFin(nouveau.getDateDebut().minusDays(1));
        ancien.setStatut(termine);
        contratRepository.saveAndFlush(ancien);

        return creer(nouveau);
    }

    @Transactional
    public Contrat creerAvecDetails(Integer idEmploye, Integer idTypeContrat, Integer idPoste,
            LocalDate dateDebut, LocalDate dateFin, BigDecimal salaireMensuel) {

        Employe employe = employeService.getById(idEmploye);

        Contrat contrat = new Contrat();
        contrat.setEmploye(employe);
        contrat.setTypeContrat(typeContratService.getById(idTypeContrat));
        contrat.setPoste(posteService.getById(idPoste));
        contrat.setDateDebut(dateDebut);
        contrat.setDateFin(dateFin);
        contrat.setSalaireMensuel(salaireMensuel);

        return creer(contrat);
    }

    @Transactional
    public Contrat renouvelerAvecDetails(Integer idAncienContrat, Integer idTypeContrat, Integer idPoste,
            LocalDate dateDebut, LocalDate dateFin, BigDecimal salaireMensuel) {

        Contrat nouveau = new Contrat();
        nouveau.setTypeContrat(typeContratService.getById(idTypeContrat));
        nouveau.setPoste(posteService.getById(idPoste));
        nouveau.setDateDebut(dateDebut);
        nouveau.setDateFin(dateFin);
        nouveau.setSalaireMensuel(salaireMensuel);

        return renouveler(idAncienContrat, nouveau);
    }

    @Transactional(readOnly = true)
    public List<Contrat> historique(Integer idEmploye) {
        return contratRepository.findByEmployeIdEmployerOrderByDateDebutDesc(idEmploye);
    }

    @Transactional(readOnly = true)
    public Optional<Contrat> contratActif(Integer idEmploye) {
        return contratRepository.findFirstByEmployeIdEmployerAndStatutLibelle(idEmploye, "actif");
    }

    private void verifierDureeContrat(Contrat contrat) {
        TypeContrat type = contrat.getTypeContrat();

        if (type.getDureeMaxMois() == null) {
            if (contrat.getDateFin() != null
                    && contrat.getDateFin().isBefore(contrat.getDateDebut())) {
                throw new IllegalArgumentException(
                        "La date de fin ne peut pas précéder la date de début.");
            }
            return;
        }

        if (contrat.getDateFin() == null) {
            throw new IllegalArgumentException(
                    "La date de fin est obligatoire pour un contrat " + type.getLibelle() + ".");
        }

        if (contrat.getDateFin().isBefore(contrat.getDateDebut())) {
            throw new IllegalArgumentException(
                    "La date de fin ne peut pas précéder la date de début.");
        }

        long mois = ChronoUnit.MONTHS.between(contrat.getDateDebut(), contrat.getDateFin());
        if (mois > type.getDureeMaxMois()) {
            throw new IllegalArgumentException(
                    "La durée du contrat " + type.getLibelle() + " ne peut pas dépasser "
                            + type.getDureeMaxMois() + " mois. Calculé : " + mois + " mois.");
        }
    }

    private void verifierSalaire(Integer idPoste, BigDecimal salaire) {
        GrilleSalarial grille = grilleSalarialService.getGrillePourPoste(idPoste)
                .orElseThrow(() -> new IllegalStateException(
                        "Aucune grille salariale pour ce poste"));
        if (salaire.compareTo(grille.getSalaireMin()) < 0
                || salaire.compareTo(grille.getSalaireMax()) > 0) {
            throw new IllegalArgumentException(
                    "Le salaire doit être compris entre "
                            + grille.getSalaireMin() + " et " + grille.getSalaireMax());
        }
    }

    private void verifierPasDeChevauchement(Integer idEmploye, LocalDate debut, LocalDate fin) {
        if (contratRepository.existeChevauchement(idEmploye, debut, fin)) {
            throw new IllegalArgumentException(
                    "Ce contrat chevauche un contrat existant de ce salarié. "
                            + "Utilisez le renouvellement ou corrigez les dates.");
        }
    }
}