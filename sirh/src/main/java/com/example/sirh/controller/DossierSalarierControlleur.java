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

        // Référentiel central
        model.addAttribute("employe", employe);
        model.addAttribute("contrats", contratRepo.findByEmployeIdEmployerOrderByDateDebutDesc(id)); // Trié par date
        model.addAttribute("documents", documentRepo.findByEmployeIdEmployerOrderByDateDepotDesc(id));
        
        // Coordonnées
        model.addAttribute("coordonnees", coordonneesRepo.findByEmploye(employe).orElse(new CoordonneesEmploye()));
        
        // Journal d'audit
        model.addAttribute("historiqueAudit", journalAuditRepo.findByEntiteCibleAndIdEntiteCibleOrderByDateActionDesc("employer", id));

        // Gestion de la validité des documents
        LocalDate today = LocalDate.now();
        List<Document> docsExpires = documentRepo.findByEmployeIdEmployerAndDateExpirationBefore(id, today);
        model.addAttribute("alerteDocuments", docsExpires);

        // Traçabilité : On trace la consultation
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
