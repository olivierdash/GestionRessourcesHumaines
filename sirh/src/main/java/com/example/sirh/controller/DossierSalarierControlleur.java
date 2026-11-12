package com.example.sirh.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.model.CoordonneesEmploye;
import com.example.sirh.model.Employe;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.DocumentRepository;
import com.example.sirh.repository.JournalAuditRepository;

import ch.qos.logback.core.model.Model;
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

    @GetMapping("/{id}")
    public String voirDossier(@PathVariable Integer id, Model model, Principal principal) {
        // 1. Récupération de l'employé
        Employe employe = employerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé introuvable avec l'ID : " + id));

        // 2. Centralisation des données (Le référentiel)
        model.addAttribute("employe", employe);
        model.addAttribute("contrats", contratRepo.findByEmployeIdEmployer(id));
        model.addAttribute("documents", documentRepo.findByEmployeIdEmployerOrderByDateDepotDesc(id));
        
        // Coordonnées (si vide, on crée un objet vide pour éviter le crash de Thymeleaf)
        model.addAttribute("coordonnees", coordonneesRepo.findByEmploye(employe).orElse(new CoordonneesEmploye()));
        
        // Historique d'audit
        model.addAttribute("historiqueAudit", journalAuditRepo.findByEntiteCibleAndIdEntiteCibleOrderByDateActionDesc("employer", id));

        // 3. Gestion de la validité des documents (Ta règle)
        LocalDate today = LocalDate.now();
        List<Document> docsExpires = documentRepo.findByEmployeIdEmployerAndDateExpirationBefore(id, today);
        model.addAttribute("alerteDocuments", docsExpires);

        // 4. Journal d'audit : On trace la consultation (Ta règle)
        auditService.enregistrerAction(principal.getName(), "CONSULTATION_DOSSIER", "employer", id, "Consultation du dossier complet");

        return "dossier/dossier-salarie"; // Le nom de la vue HTML
    }

    @PostMapping("/{id}/coordonnees")
    public String updateCoordonnees(@PathVariable Integer id, 
                                    @ModelAttribute CoordonneesEmploye coords, 
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        Employe employe = employerRepo.findById(id).orElseThrow();
        CoordonneesEmploye existantes = coordonneesRepo.findByEmploye(employe).orElse(new CoordonneesEmploye());
        
        // Garder l'ancienne adresse pour l'audit
        String ancienneAdresse = existantes.getAdresse();

        // Mise à jour
        existantes.setEmploye(employe);
        existantes.setAdresse(coords.getAdresse());
        existantes.setVille(coords.getVille());
        existantes.setCodePostal(coords.getCodePostal());
        existantes.setSituationMatrimoniale(coords.getSituationMatrimoniale());
        existantes.setTelephonePersonnel(coords.getTelephonePersonnel());
        
        coordonneesRepo.save(existantes);

        // Journal d'audit : On trace la modification
        String details = "Adresse modifiée : " + ancienneAdresse + " -> " + coords.getAdresse();
        auditService.enregistrerAction(principal.getName(), "MODIFICATION_COORDONNEES", "employer", id, details);

        redirectAttributes.addFlashAttribute("success", "Coordonnées mises à jour !");
        return "redirect:/dossier/" + id;
    }

}
