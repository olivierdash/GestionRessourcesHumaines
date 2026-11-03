package com.example.sirh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.model.Role;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.service.RoleService;
import com.example.sirh.service.UtilisateurService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/utilisateurs")
@RequiredArgsConstructor 
public class AdminUtilisateurController {

    private final UtilisateurService utilisateurService; 
    private final RoleService roleService;               

    @GetMapping
    public String index(Model model) {
        // Initialisation d'un objet vide avec un rôle vide pour le binding Thymeleaf
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setRole(new Role());

        model.addAttribute("utilisateurs", utilisateurService.getAllUtilisateurs());
        model.addAttribute("roles", roleService.getRoles());
        model.addAttribute("utilisateur", utilisateur);
        return "admin/utilisateurs";
    }

    @PostMapping
    public String store(@ModelAttribute Utilisateur utilisateur, RedirectAttributes redirect) {
        try {
            utilisateurService.createUtilisateur(utilisateur);
            redirect.addFlashAttribute("success", "Utilisateur créé avec succès");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirect.addFlashAttribute("error", "Erreur : cet email est déjà utilisé.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/utilisateurs";
    }
}