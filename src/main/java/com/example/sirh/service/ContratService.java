package com.example.sirh.service;

import com.example.sirh.model.Contrat;
import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.model.StatutContrat;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.GrilleSalarialRepository;
import com.example.sirh.repository.StatutContratRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ContratService {

    private final ContratRepository contratRepository;
    private final GrilleSalarialRepository grilleSalarialRepository;
    private final StatutContratRepository statutContratRepository;

    public ContratService(ContratRepository contratRepository,
            GrilleSalarialRepository grilleSalarialRepository,
            StatutContratRepository statutContratRepository) {
        this.contratRepository = contratRepository;
        this.grilleSalarialRepository = grilleSalarialRepository;
        this.statutContratRepository = statutContratRepository;
    }

    public Contrat trouver(Integer idContrat) {
        return contratRepository.findById(idContrat)
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable"));
    }

    @Transactional
    public Contrat creer(Contrat contrat) {
        Integer idEmploye = contrat.getEmploye().getIdEmployer();

        verifierDatesCdd(contrat);
        verifierSalaire(contrat.getPoste().getIdPoste(), contrat.getSalaireMensuel());
        verifierPasDeChevauchement(idEmploye, contrat.getDateDebut(), contrat.getDateFin());

        StatutContrat actif = statutContratRepository.findByLibelle("actif")
                .orElseThrow(() -> new IllegalStateException("Statut 'actif' introuvable"));
        contrat.setStatut(actif);

        return contratRepository.save(contrat);
    }

    @Transactional
    public Contrat renouveler(Integer idAncienContrat, Contrat nouveau) {
        Contrat ancien = contratRepository.findById(idAncienContrat)
                .orElseThrow(() -> new IllegalArgumentException("Contrat introuvable"));

        StatutContrat termine = statutContratRepository.findByLibelle("terminé")
                .orElseThrow(() -> new IllegalStateException("Statut 'terminé' introuvable"));

        // Le nouveau contrat est pour le même salarié
        nouveau.setEmploye(ancien.getEmploye());

        // 1. Clôturer l'ancien : il finit la veille du nouveau
        ancien.setDateFin(nouveau.getDateDebut().minusDays(1));
        ancien.setStatut(termine);
        contratRepository.saveAndFlush(ancien);

        // 2. Créer le nouveau (les 3 règles sont vérifiées dans creer)
        return creer(nouveau);
    }

    private void verifierDatesCdd(Contrat contrat) {
        boolean estCdd = "CDD".equals(contrat.getTypeContrat().getLibelle());

        if (estCdd && contrat.getDateFin() == null) {
            throw new IllegalArgumentException("La date de fin est obligatoire pour un CDD");
        }
        if (contrat.getDateFin() != null
                && contrat.getDateFin().isBefore(contrat.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas précéder la date de début");
        }
    }

    private void verifierSalaire(Integer idPoste, BigDecimal salaire) {
        GrilleSalarial grille = grilleSalarialRepository.findFirstByPosteIdPoste(idPoste)
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
                    "Ce contrat chevauche un contrat existant de ce salarié");
        }
    }

    // Page 17 : l'historique
    public List<Contrat> historique(Integer idEmploye) {
        return contratRepository.findByEmployeIdEmployerOrderByDateDebutDesc(idEmploye);
    }

    // Page 17 : le contrat en cours
    public Optional<Contrat> contratActif(Integer idEmploye) {
        return contratRepository.findFirstByEmployeIdEmployerAndStatutLibelle(idEmploye, "actif");
    }
}