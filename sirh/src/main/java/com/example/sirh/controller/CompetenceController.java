package com.example.sirh.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.model.Competence;
import com.example.sirh.repository.CompetenceRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/developpement/competences")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'DG', 'MANAGER')")
public class CompetenceController {

    private final CompetenceRepository competenceRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("competences", competenceRepository.findAll());
        model.addAttribute("competence", new Competence());
        return "developpement/competences";
    }

    @PostMapping
    public String enregistrer(Competence competence,
            RedirectAttributes redirectAttributes) {
        competenceRepository.save(competence);
        redirectAttributes.addFlashAttribute("success", "Compétence enregistrée.");
        return "redirect:/developpement/competences";
    }
}
