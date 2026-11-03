package com.example.sirh.controller;

import com.example.sirh.model.Employe;
import com.example.sirh.service.AffectationService;
import com.example.sirh.service.CandidatureService;
import com.example.sirh.service.ContratService;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.EmbaucheService;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/employes")
@AllArgsConstructor
public class EmployeController {

    private final EmployeService employeService;
    private final CandidatureService candidatureService;
    private final ContratService contratService;
    private final AffectationService affectationService;

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer idDepartement,
            Model model) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100),
                Sort.by("hiringDate").descending());

        Page<Employe> employesPage = employeService.getAllEmployes(pageable);

        model.addAttribute("employes", employesPage);
        model.addAttribute("idDepartement", idDepartement);

        return "employe/index";
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
        model.addAttribute("employe", employeService.getById(id));
        // model.addAttribute("contrats", embaucheService.contratsDe(id));
        // model.addAttribute("affectations", embaucheService.affectationsDe(id));
        return "employe/fiche";
    }
}