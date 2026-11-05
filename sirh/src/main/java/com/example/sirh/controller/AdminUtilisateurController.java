package com.example.sirh.controller;

import com.example.sirh.config.CustomUserDetails;
import com.example.sirh.dto.LoginChangeDTO;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Role;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.service.RoleService;
import com.example.sirh.service.UtilisateurService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin/utilisateurs")
@RequiredArgsConstructor
public class AdminUtilisateurController {

    private final UtilisateurService utilisateurService;
    private final RoleService roleService;

    // =========================================================
    // LISTE (GET)
    // =========================================================

    @GetMapping
    public String index(@ModelAttribute("utilisateur") Utilisateur utilisateur, Model model) {
        if (utilisateur.getRole() == null) {
            utilisateur.setRole(new Role());
        }

        model.addAttribute("utilisateurs", utilisateurService.getAllUtilisateurs());
        model.addAttribute("roles", roleService.getRoles());
        model.addAttribute("utilisateur", utilisateur);
        return "admin/utilisateurs";
    }

    // =========================================================
    // CRÉATION (POST)
    // =========================================================

    @PostMapping
    public String store(
            @ModelAttribute Utilisateur utilisateur,
            @RequestParam(required = false) String codeSecret,
            RedirectAttributes redirect) {
        try {
            utilisateurService.createUtilisateur(utilisateur, codeSecret);
            redirect.addFlashAttribute("success", "Utilisateur créé avec succès");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("errors", e.getErrors());
            redirect.addFlashAttribute("errorMessage", "Veuillez corriger les erreurs ci-dessous.");
            redirect.addFlashAttribute("utilisateur", utilisateur);
            redirect.addFlashAttribute("openUserModalFor", true);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirect.addFlashAttribute("errorMessage", "Erreur : cet email est déjà utilisé.");
            redirect.addFlashAttribute("utilisateur", utilisateur);
            redirect.addFlashAttribute("openUserModalFor", true);
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la création d'un utilisateur : {}", e.getMessage());
            redirect.addFlashAttribute("errorMessage", "Erreur inattendue lors de la création.");
            redirect.addFlashAttribute("utilisateur", utilisateur);
            redirect.addFlashAttribute("openUserModalFor", true);
        }
        return "redirect:/admin/utilisateurs";
    }

    // =========================================================
    // CHANGEMENT DE MOT DE PASSE (POST)
    // =========================================================

    @PostMapping("/{id}/password")
    public String changerMotDePasse(
            @PathVariable Integer id,
            @ModelAttribute("changeLogin") LoginChangeDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirect) {

        request.setIdAdminConnecte(userDetails != null ? userDetails.getUtilisateur().getIdUtilisateur() : null);

        try {
            utilisateurService.changerMotDePasse(request);
            redirect.addFlashAttribute("success", "Mot de passe modifié avec succès.");
        } catch (ValidationMetierException e) {
            redirect.addFlashAttribute("errors", e.getErrors());
            redirect.addFlashAttribute("errorMessage", "Veuillez corriger les erreurs ci-dessous.");
            redirect.addFlashAttribute("openPasswordModalFor", id);
            redirect.addFlashAttribute("nouveauMotDePasse", request.getNouveauMotDePasse());
        } catch (Exception e) {
            log.error("Erreur inattendue lors du changement de mot de passe : {}", e.getMessage());
            redirect.addFlashAttribute("errorMessage", "Erreur lors de la modification du mot de passe.");
        }
        return "redirect:/admin/utilisateurs";
    }
}