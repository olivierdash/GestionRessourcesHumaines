package com.example.sirh.controller;

import com.example.sirh.model.Candidature;
import com.example.sirh.model.Employe;
import com.example.sirh.repository.CandidatureRepository;
import com.example.sirh.repository.EmployeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/employes")
public class EmployeController {

    private final EmployeRepository employeRepository;
    private final CandidatureRepository candidatureRepository;

    public EmployeController(EmployeRepository employeRepository, CandidatureRepository candidatureRepository) {
        this.employeRepository = employeRepository;
        this.candidatureRepository = candidatureRepository;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer idDepartement,
            Model model) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100),
                Sort.by("hiringDate").descending());

        Page<Employe> employesPage = employeRepository.findAll(pageable);

        model.addAttribute("employes", employesPage);
        model.addAttribute("idDepartement", idDepartement);

        return "employe/index";
    }

    // -----------------------------------------------------------------
    // CREATE (GET) : Afficher le formulaire
    // -----------------------------------------------------------------
    @GetMapping("/create")
    public String create(Model model) {
        // On récupère toutes les candidatures (Idéalement, il faudrait filtrer celles qui n'ont pas encore d'employé)
        List<Candidature> candidatures = candidatureRepository.findCandidaturesSansEmploye();
        
        model.addAttribute("candidatures", candidatures);
        return "employe/form";
    }

    // -----------------------------------------------------------------
    // CREATE (POST) : Enregistrer le nouvel employé
    // -----------------------------------------------------------------
    @PostMapping
    public String store(
            @RequestParam Integer idCandidature,
            @RequestParam String matricule,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hiringDate,
            @RequestParam String statusEmployer) {

        Candidature candidature = candidatureRepository.findById(idCandidature)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidature introuvable"));

        Employe employe = Employe.builder()
                .candidature(candidature)
                .matricule(matricule)
                .hiringDate(hiringDate)
                .statusEmployer(statusEmployer)
                .build();

        employeRepository.save(employe);

        return "redirect:/employes";
    }
}