package com.example.sirh.controller;

import com.example.sirh.model.Departement;
import com.example.sirh.service.DepartementService;
import com.example.sirh.service.UtilisateurService;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/departements")
@AllArgsConstructor
public class DepartementController {

    // On injecte uniquement les Services
    private final DepartementService departementService;
    private final UtilisateurService utilisateurService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("departements", departementService.getAllDepartements());
        return "departement/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("managers", utilisateurService.getManagers());
        model.addAttribute("departement", new Departement());
        return "departement/form";
    }

    @PostMapping
    public String store(
            @RequestParam String titre,
            @RequestParam(required = false) Integer idManager,
            Model model) {
        try {
            departementService.createDepartement(titre, idManager);
            return "redirect:/departements";
        } catch (IllegalArgumentException e) {
            // En cas d'erreur (ex: titre déjà existant)
            model.addAttribute("error", e.getMessage());
            model.addAttribute("managers", utilisateurService.getManagers());
            
            // On garde la saisie de l'utilisateur
            Departement dep = new Departement();
            dep.setTitre(titre);
            model.addAttribute("departement", dep);
            
            return "departement/form";
        }
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Integer id, Model model) {
        Departement departement = departementService.getById(id);
        
        // Appel au service pour compter les employés
        long nombreEmployes = departementService.countEmployesByDepartement(id);

        model.addAttribute("departement", departement);
        model.addAttribute("nombreEmployes", nombreEmployes);

        return "departement/show";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        Departement departement = departementService.getById(id);
        model.addAttribute("managers", utilisateurService.getManagers());
        model.addAttribute("departement", departement);
        return "departement/form";
    }

    @PutMapping("/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String titre,
            @RequestParam(required = false) Integer idManager,
            Model model) {
        try {
            departementService.updateDepartement(id, titre, idManager);
            return "redirect:/departements";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("managers", utilisateurService.getManagers());
            
            // On garde les modifications temporaires
            Departement dep = departementService.getById(id);
            dep.setTitre(titre); 
            model.addAttribute("departement", dep);
            
            return "departement/form";
        }
    }

    @GetMapping("/check-titre")
    @ResponseBody
    public boolean checkTitreDisponible(
            @RequestParam String titre,
            @RequestParam(required = false) Integer idDepartement) {
        // Le service gère la logique de vérification
        return departementService.isTitreDisponible(titre, idDepartement);
    }
}