package com.example.sirh.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.model.EmployeCompetence;
import com.example.sirh.repository.CompetenceRepository;
import com.example.sirh.repository.EmployeCompetenceRepository;
import com.example.sirh.repository.EmployeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/developpement/employes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'DG', 'MANAGER')")
public class EmployeCompetenceController {

    private final EmployeCompetenceRepository employeCompetenceRepository;
    private final CompetenceRepository competenceRepository;
    private final EmployeRepository employeRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("competencesEmployes", employeCompetenceRepository.findAll());
        model.addAttribute("competences", competenceRepository.findAll());
        model.addAttribute("employes", employeRepository.findAll());
        return "developpement/employes";
    }

    @PostMapping
    public String enregistrer(
            @RequestParam Integer idEmploye,
            @RequestParam Integer idCompetence,
            @RequestParam Integer niveauAcquis,
            @RequestParam(required = false) String sourceEvaluation,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateAcquisition,
            @RequestParam(required = false) String commentaire,
            RedirectAttributes redirectAttributes) {

        EmployeCompetence employeCompetence = EmployeCompetence.builder()
                .employe(employeRepository.findById(idEmploye).orElseThrow())
                .competence(competenceRepository.findById(idCompetence).orElseThrow())
                .niveauAcquis(niveauAcquis)
                .dateAcquisition(dateAcquisition != null ? dateAcquisition : LocalDate.now())
                .sourceEvaluation(sourceEvaluation)
                .commentaire(commentaire)
                .build();

        employeCompetenceRepository.save(employeCompetence);
        redirectAttributes.addFlashAttribute("success", "Compétence employé enregistrée.");
        return "redirect:/developpement/employes";
    }
}
