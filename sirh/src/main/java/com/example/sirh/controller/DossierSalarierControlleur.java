package com.example.sirh.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.model.CoordonneesEmploye;
import com.example.sirh.model.Document;
import com.example.sirh.model.Employe;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.DocumentRepository;
import com.example.sirh.repository.JournalAuditRepository;
import com.example.sirh.repository.CoordonneesEmployeRepository;
import com.example.sirh.service.AuditService;

import jakarta.transaction.Transactional;

import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;

@Controller 
@RequestMapping ("/dossier")
@RequiredArgsConstructor 
public class DossierSalarierControlleur {
    private final EmployeRepository employerRepo;
    private final ContratRepository contratRepo;
    private final DocumentRepository documentRepo;
    private final JournalAuditRepository journalAuditRepo;
    private final CoordonneesEmployeRepository coordonneesRepo;
    private final AuditService auditService;

        @GetMapping("/{id}")
    public String voirDossier(@PathVariable Integer id, Model model, Principal principal) {
        Employe employe = employerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé introuvable avec l'ID : " + id));

        // 1. RÉFÉRENTIEL CENTRAL (Ton travail)
        model.addAttribute("employe", employe);
        model.addAttribute("coordonnees", coordonneesRepo.findByEmploye(employe).orElse(new CoordonneesEmploye()));
        model.addAttribute("documents", documentRepo.findByEmployeIdEmployerOrderByDateDepotDesc(id));
        
        // 2. HISTORIQUE DE CARRIÈRE (Travail du binôme)
        List<com.example.sirh.model.Contrat> tousLesContrats = contratRepo.findByEmployeIdEmployerOrderByDateDebutDesc(id);
        model.addAttribute("contrats", tousLesContrats); // Pour la timeline HTML

        // 3. SITUATION PROFESSIONNELLE ACTUELLE (Travail du binôme)
        // On cherche le premier contrat avec le statut "Actif"
        com.example.sirh.model.Contrat contratActuel = tousLesContrats.stream()
                .filter(c -> c.getStatut() != null && "Actif".equalsIgnoreCase(c.getStatut().getLibelle()))
                .findFirst()
                .orElse(null);
        model.addAttribute("contratActuel", contratActuel); // Pour les petites cartes en haut

        // 4. GESTION DE LA VALIDITÉ DES DOCUMENTS (Ton travail - à décommenter)
        LocalDate today = LocalDate.now();
        List<Document> docsExpires = documentRepo.findByEmployeIdEmployerAndDateExpirationBefore(id, today);
        model.addAttribute("alerteDocuments", docsExpires);

        // 5. JOURNAL D'AUDIT (Ton travail)
        model.addAttribute("historiqueAudit", journalAuditRepo.findByEntiteCibleAndIdEntiteCibleOrderByDateActionDesc("employer", id));
        auditService.enregistrerAction(principal.getName(), "CONSULTATION_DOSSIER", "employer", id, "Consultation du dossier complet");

        return "dossier/dossier-salarie";
    }

    @PostMapping("/{id}/coordonnees")
    @Transactional 
    public String updateCoordonnees(@PathVariable Integer id, 
                                    @ModelAttribute CoordonneesEmploye coords, 
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        Employe employe = employerRepo.findById(id).orElseThrow();
        CoordonneesEmploye existantes = coordonneesRepo.findByEmploye(employe).orElse(new CoordonneesEmploye());
        
        String ancienneAdresse = existantes.getAdresse() != null ? existantes.getAdresse() : "Non renseignée";

        // Mise à jour (CORRECTION : Ajout du nombre d'enfants)
        existantes.setEmploye(employe);
        existantes.setAdresse(coords.getAdresse());
        existantes.setVille(coords.getVille());
        existantes.setCodePostal(coords.getCodePostal());
        existantes.setSituationMatrimoniale(coords.getSituationMatrimoniale());
        existantes.setTelephonePersonnel(coords.getTelephonePersonnel());
        existantes.setNombreEnfants(coords.getNombreEnfants()); // LIGNE AJOUTÉE
        
        coordonneesRepo.save(existantes);

        // Journal d'audit
        String details = "Adresse modifiée : " + ancienneAdresse + " -> " + coords.getAdresse();
        auditService.enregistrerAction(principal.getName(), "MODIFICATION_COORDONNEES", "employer", id, details);

        redirectAttributes.addFlashAttribute("success", "Coordonnées mises à jour avec succès !");
        return "redirect:/dossier/" + id;
    }

}
