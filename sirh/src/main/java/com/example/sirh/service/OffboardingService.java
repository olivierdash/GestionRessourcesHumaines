package com.example.sirh.service;

import com.example.sirh.model.RuptureContrat;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.RuptureContratRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * @author moi
 * module 12
 * 
 * Orchestrateur de la clôture d'offboarding.
 * RÈGLES cumulées avant CLOTUREE :
 *   1. tous les accès désactivés
 *   2. tout le matériel restitué
 *   3. les 3 documents de sortie remis
 *   4. le STC validé
 * Puis archivage du dossier employé.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OffboardingService {

    private final RuptureContratRepository ruptureRepo;
    private final EmployeRepository employeRepo;
    private final DesactivationAccesService accesService;
    private final MaterielService materielService;
    private final DocumentSortieService documentService;
    private final SoldeToutCompteService stcService;
    private final RuptureContratService ruptureService;

    /** Étape 1 : on entre en offboarding (checklist + génération des documents). */
    public RuptureContrat demarrerOffboarding(Integer idRupture, Utilisateur acteur) {
        RuptureContrat r = ruptureService.findById(idRupture);
        if (!"INITIEE".equals(r.getStatutRupture()) && !"PREAVIS_EN_COURS".equals(r.getStatutRupture()))
            throw new IllegalStateException("L'offboarding ne peut être démarré depuis le statut " + r.getStatutRupture());

        if ("INITIEE".equals(r.getStatutRupture()))
            ruptureService.changerStatut(idRupture, "PREAVIS_EN_COURS");
        ruptureService.changerStatut(idRupture, "OFFBOARDING_EN_COURS");

        accesService.initChecklist(r);
        documentService.genererObligatoires(r, acteur);
        return r;
    }

    /** Étape 2 : clôture. Vérifie toutes les règles avant de passer en CLOTUREE + archiver. */
    public RuptureContrat cloturer(Integer idRupture) {
        RuptureContrat r = ruptureService.findById(idRupture);
        Integer idEmploye = r.getEmploye().getIdEmployer();

        if (!accesService.toutEstDesactive(idRupture))
            throw new IllegalStateException("Accès non tous désactivés");
        if (!materielService.toutEstRestitue(idEmploye))
            throw new IllegalStateException("Matériel non restitué");
        if (!documentService.tousRemis(idRupture))
            throw new IllegalStateException("Documents de sortie non tous remis");
        var stc = stcService.findByrupture(idRupture)
                .orElseThrow(() -> new IllegalStateException("Solde de tout compte manquant"));
        if (!"VALIDE".equals(stc.getStatut()) && !"PAYE".equals(stc.getStatut()))
            throw new IllegalStateException("Le solde de tout compte doit être validé");

        // Passage en CLOTUREE + archivage du dossier employé
        ruptureService.changerStatut(idRupture, "CLOTUREE");
        ruptureService.archiver(idRupture);

        var employe = r.getEmploye();
        employe.setArchive(true);
        employe.setDateArchivage(LocalDate.now());
        employeRepo.save(employe);

        return r;
    }
}