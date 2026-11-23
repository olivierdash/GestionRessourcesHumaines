package com.example.sirh.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.sirh.repository.CompetenceRepository;
import com.example.sirh.repository.EmployeCompetenceRepository;
import com.example.sirh.repository.EvaluationRepository;
import com.example.sirh.repository.FormationRepository;
import com.example.sirh.repository.ObjectifRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/developpement")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'DG', 'MANAGER')")
public class DeveloppementController {

    private final CompetenceRepository competenceRepository;
    private final EmployeCompetenceRepository employeCompetenceRepository;
    private final ObjectifRepository objectifRepository;
    private final FormationRepository formationRepository;
    private final EvaluationRepository evaluationRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("nombreCompetences", competenceRepository.count());
        model.addAttribute("nombreCompetencesEmployes", employeCompetenceRepository.count());
        model.addAttribute("nombreObjectifs", objectifRepository.count());
        model.addAttribute("nombreEvaluations", evaluationRepository.count());
        model.addAttribute("nombreFormations", formationRepository.count());
        return "developpement/index";
    }
}
