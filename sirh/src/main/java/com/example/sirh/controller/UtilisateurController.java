package com.example.sirh.controller;

import com.example.sirh.config.CustomUserDetails;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.service.DashboardService;
import com.example.sirh.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/utilisateur")
@RequiredArgsConstructor
public class UtilisateurController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit,
            Model model) {

        Utilisateur utilisateur = userDetails.getUtilisateur();

        // --- LOGS DE DEBUGGING ---


        // Redirection si ADMIN
        if ("ADMIN".equalsIgnoreCase(utilisateur.getRole().getLibelle())) {
            return "redirect:/admin/utilisateurs";
        }

        // Clamp pour éviter les abus (ex: ?limit=99999)
        int safeLimit = Math.max(1, Math.min(limit, 100));

        model.addAttribute("dashboard", dashboardService.getDashboardData(utilisateur, safeLimit));
        return "utilisateur/dashboard";
    }
}