package com.example.sirh.controller;

import com.example.sirh.model.RuptureContrat;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller("offboardingMvcController")
@RequestMapping("/offboarding")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH','DG')")
public class OffboardingController {
    private final RuptureContratService ruptureService;
    private final OffboardingService offboardingService;
    private final MaterielService materielService;
    private final DesactivationAccesService accesService;
    private final DocumentSortieService documentService;
    private final SoldeToutCompteService stcService;
    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;
    private final CongeAccessService accessService;
    private final AuditService auditService;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("ruptures", ruptureService.listerActifs());
        model.addAttribute("employes", employeRepository.findAll());
        model.addAttribute("contrats", contratRepository.findAll());
        return "offboarding/index";
    }

    @PostMapping("/ruptures")
    public String initier(@RequestParam Integer idEmployer,
                          @RequestParam Integer idContract,
                          @RequestParam String motifRupture,
                          @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateNotification,
                          @RequestParam(defaultValue="0") Integer dureePreavisJours,
                          @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dateSortie,
                          @RequestParam(required=false) String commentaire,
                          RedirectAttributes redirect) {
        try {
            var employe = employeRepository.findById(idEmployer).orElseThrow();
            var contrat = contratRepository.findById(idContract).orElseThrow();
            if (!employe.getIdEmployer().equals(contrat.getEmploye().getIdEmployer()))
                throw new IllegalArgumentException("Le contrat ne correspond pas au salarié");
            Utilisateur acteur = accessService.utilisateurCourant();
            auditService.definirUtilisateurCourant(acteur);
            RuptureContrat rupture = RuptureContrat.builder()
                    .employe(employe).contrat(contrat).motifRupture(motifRupture)
                    .dateNotification(dateNotification).dureePreavisJours(dureePreavisJours)
                    .dateSortie(dateSortie).commentaire(commentaire).initiePar(acteur).build();
            ruptureService.initier(rupture);
            redirect.addFlashAttribute("succes", "Rupture initiée.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/offboarding";
    }

    @GetMapping("/ruptures/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        RuptureContrat rupture = ruptureService.findById(id);
        model.addAttribute("rupture", rupture);
        model.addAttribute("materiels", materielService.listerParEmploye(rupture.getEmploye().getIdEmployer()));
        model.addAttribute("acces", accesService.lister(id));
        model.addAttribute("documents", documentService.lister(id));
        model.addAttribute("stc", stcService.findByrupture(id).orElse(null));
        return "offboarding/detail";
    }

    @PostMapping("/ruptures/{id}/demarrer")
    public String demarrer(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            offboardingService.demarrerOffboarding(id, accessService.utilisateurCourant());
            redirect.addFlashAttribute("succes", "Offboarding démarré.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/offboarding/ruptures/" + id;
    }

    @PostMapping("/ruptures/{id}/cloturer")
    public String cloturer(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            offboardingService.cloturer(id);
            redirect.addFlashAttribute("succes", "Offboarding clôturé.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/offboarding/ruptures/" + id;
    }

    @PostMapping("/ruptures/{id}/solde")
    public String creerSolde(@PathVariable Integer id,
                             @RequestParam BigDecimal salaireMensuelReference,
                             @RequestParam(defaultValue="0") BigDecimal joursCongesRestants,
                             @RequestParam(defaultValue="0") BigDecimal totalGains,
                             @RequestParam(defaultValue="0") BigDecimal totalRetenues,
                             RedirectAttributes redirect) {
        try {
            stcService.creer(com.example.sirh.model.SoldeToutCompte.builder()
                    .ruptureContrat(ruptureService.findById(id))
                    .salaireMensuelReference(salaireMensuelReference)
                    .joursCongesRestants(joursCongesRestants)
                    .totalGains(totalGains).totalRetenues(totalRetenues).build());
            redirect.addFlashAttribute("succes", "Solde de tout compte créé.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/offboarding/ruptures/" + id;
    }
}
