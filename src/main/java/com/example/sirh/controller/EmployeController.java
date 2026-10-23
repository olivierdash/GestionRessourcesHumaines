package com.example.sirh.controller;

import com.example.sirh.model.Employe;
import com.example.sirh.repository.EmployeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/employes")
public class EmployeController {

    private final EmployeRepository employeRepository;

    public EmployeController(EmployeRepository employeRepository) {
        this.employeRepository = employeRepository;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer idDepartement, // Récupéré depuis le lien de la fiche département
            Model model) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100),
                Sort.by("hiringDate").descending());

        Page<Employe> employesPage = employeRepository.findAll(pageable);

        model.addAttribute("employes", employesPage);
        
        model.addAttribute("idDepartement", idDepartement);

        return "employe/index";
    }
}