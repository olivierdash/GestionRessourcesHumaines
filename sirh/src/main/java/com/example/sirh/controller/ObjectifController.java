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

import com.example.sirh.model.Objectif;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.repository.ObjectifRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/developpement/objectifs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'DG', 'MANAGER')")
public class ObjectifController {

    private final ObjectifRepository objectifRepository;
    private final EmployeRepository employeRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("objectifs", objectifRepository.findAll());
        model.addAttribute("employes", employeRepository.findAll());
        return "developpement/objectifs";
    }

    @PostMapping
    public String enregistrer(
            @RequestParam Integer idEmploye,
            @RequestParam String titre,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "100") java.math.BigDecimal poids,
            RedirectAttributes redirectAttributes) {

        Objectif objectif = Objectif.builder()
                .employe(employeRepository.findById(idEmploye).orElseThrow())
                .titre(titre)
                .description(description)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .poids(poids)
                .statut("DEFINI")
                .build();

        objectifRepository.save(objectif);
        redirectAttributes.addFlashAttribute("success", "Objectif enregistré.");
        return "redirect:/developpement/objectifs";
    }
}
