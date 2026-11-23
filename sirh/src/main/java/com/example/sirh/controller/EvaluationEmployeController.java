package com.example.sirh.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.sirh.repository.EvaluationRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/developpement/evaluations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RH', 'DG', 'MANAGER')")
public class EvaluationEmployeController {

    private final EvaluationRepository evaluationRepository;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("evaluations", evaluationRepository.findAll());
        return "developpement/evaluations";
    }
}
