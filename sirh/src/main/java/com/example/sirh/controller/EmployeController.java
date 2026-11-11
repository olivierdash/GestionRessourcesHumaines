package com.example.sirh.controller;

import com.example.sirh.dto.EmployeFilter;
import com.example.sirh.model.Affectation;
import com.example.sirh.model.Contrat;
import com.example.sirh.model.Employe;
import com.example.sirh.service.AffectationService;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.ContratService;
import com.example.sirh.service.DepartementService;
import com.example.sirh.service.DocumentService;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.StatutEmployeService;
import com.example.sirh.service.EmbaucheService;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employes")
@AllArgsConstructor
public class EmployeController {

    private final EmployeService employeService;
    private final CandidatureService candidatureService;
    private final DepartementService departementService;
    private final ContratService contratService;
    private final DocumentService documentService;
    private final StatutEmployeService statutEmployeService;
    private final AffectationService affectationService;

    @GetMapping
    public String index(
            @ModelAttribute("filter") EmployeFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100),
                Sort.by("hiringDate").descending());

        Page<Employe> employesPage = employeService.getEmployesFiltres(filter, pageable);

        model.addAttribute("employes", employesPage);
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("statuts", statutEmployeService.getAll());

        return "employe/index"; // ou "employes/index" selon ton arborescence
    }

    // -----------------------------------------------------------------
    // CREATE (GET) : Afficher le formulaire
    // -----------------------------------------------------------------
    @GetMapping("/create")
    public String create(Model model) {
        // Appel au service de candidature
        model.addAttribute("candidatures", candidatureService.getCandidaturesSansEmploye());
        return "employe/form";
    }

    /** Fiche salarié avec matricule, contrat et affectation. */
    @GetMapping("/{id}")
    public String show(@PathVariable("id") Integer id, Model model) {
        Employe employe = employeService.getById(id);

        List<Contrat> contrats = contratService.historique(id); // déjà trié DESC
        List<Affectation> affectations = affectationService.historique(id);

        // Affectation active : celle dont dateFin == null
        Affectation affectationActive = affectations.stream()
                .filter(a -> a.getDateFin() == null)
                .findFirst()
                .orElse(null);

        // Contrat actif : via le service (déjà présent dans ContratService)
        Contrat contratActif = contratService.contratActif(id).orElse(null);

        model.addAttribute("employe", employe);
        model.addAttribute("contrats", contrats);
        model.addAttribute("affectations", affectations);
        model.addAttribute("affectationActive", affectationActive);
        model.addAttribute("contratActif", contratActif);
        model.addAttribute("documents", documentService.documentsDe(id));

        return "employe/fiche";
    }
}