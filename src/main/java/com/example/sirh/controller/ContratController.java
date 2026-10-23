package com.example.sirh.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.model.Contrat;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.PosteRepository;
import com.example.sirh.repository.TypeContractRepository;
import com.example.sirh.service.ContratService;

@Controller
@RequestMapping("/contrats")
public class ContratController {

    private final ContratService contratService;

    private final TypeContractRepository typeContratRepository;
    private final PosteRepository posteRepository;
    private final EmployeRepository employeRepository;

    public ContratController(ContratService contratService,
            TypeContractRepository typeContratRepository,
            PosteRepository posteRepository,
            EmployeRepository employeRepository) {
        this.contratService = contratService;
        this.typeContratRepository = typeContratRepository;
        this.posteRepository = posteRepository;
        this.employeRepository = employeRepository;
    }

    @PostMapping("/{idContrat}/renouveler")
    public String renouveler(@PathVariable Integer idContrat,
            @RequestParam Integer idTypeContrat,
            @RequestParam Integer idPoste,
            @RequestParam LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            @RequestParam BigDecimal salaireMensuel,
            RedirectAttributes redirect) {
        Integer idEmploye = contratService.trouver(idContrat).getEmploye().getIdEmployer();
        try {
            Contrat nouveau = new Contrat();
            nouveau.setTypeContrat(typeContratRepository.findById(idTypeContrat)
                    .orElseThrow(() -> new IllegalArgumentException("Type introuvable")));
            nouveau.setPoste(posteRepository.findById(idPoste)
                    .orElseThrow(() -> new IllegalArgumentException("Poste introuvable")));
            nouveau.setDateDebut(dateDebut);
            nouveau.setDateFin(dateFin);
            nouveau.setSalaireMensuel(salaireMensuel);

            contratService.renouveler(idContrat, nouveau);
            redirect.addFlashAttribute("succes", "Contrat renouvelé");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/contrats/" + idContrat + "/renouveler";
        }
        return "redirect:/contrats/employe/" + idEmploye;
    }

    // Page 18 : formulaire de renouvellement
    @GetMapping("/{idContrat}/renouveler")
    public String formRenouveler(@PathVariable Integer idContrat, Model model) {
        Contrat ancien = contratService.trouver(idContrat);

        model.addAttribute("idEmploye", ancien.getEmploye().getIdEmployer());
        model.addAttribute("ancien", ancien);
        model.addAttribute("types", typeContratRepository.findAll());
        model.addAttribute("postes", posteRepository.findAll());
        return "bloc4/contrats/form";
    }

    @PostMapping("/employe/{id}")
    public String creer(@PathVariable Integer id,
            @RequestParam Integer idTypeContrat,
            @RequestParam Integer idPoste,
            @RequestParam LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            @RequestParam BigDecimal salaireMensuel,
            RedirectAttributes redirect) {
        try {
            Contrat contrat = new Contrat();
            contrat.setEmploye(employeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Salarié introuvable")));
            contrat.setTypeContrat(typeContratRepository.findById(idTypeContrat)
                    .orElseThrow(() -> new IllegalArgumentException("Type introuvable")));
            contrat.setPoste(posteRepository.findById(idPoste)
                    .orElseThrow(() -> new IllegalArgumentException("Poste introuvable")));
            contrat.setDateDebut(dateDebut);
            contrat.setDateFin(dateFin);
            contrat.setSalaireMensuel(salaireMensuel);

            contratService.creer(contrat);
            redirect.addFlashAttribute("succes", "Contrat enregistré");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/contrats/employe/" + id + "/nouveau";
        }
        return "redirect:/contrats/employe/" + id;
    }

    // Page 18 : formulaire de création
    @GetMapping("/employe/{id}/nouveau")
    public String nouveau(@PathVariable Integer id, Model model) {
        model.addAttribute("idEmploye", id);
        model.addAttribute("types", typeContratRepository.findAll());
        model.addAttribute("postes", posteRepository.findAll());
        return "bloc4/contrats/form";
    }

    // Page 17 : historique des contrats d'un salarié
    @GetMapping("/employe/{id}")
    public String historique(@PathVariable Integer id, Model model) {
        model.addAttribute("idEmploye", id);
        model.addAttribute("contrats", contratService.historique(id));
        model.addAttribute("contratActif", contratService.contratActif(id).orElse(null));
        return "bloc4/contrats/historique";
    }
}