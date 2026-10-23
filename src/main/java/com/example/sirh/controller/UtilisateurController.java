package com.example.sirh.controller;

import com.example.sirh.model.Role;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/utilisateur")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "pages/dashboard"; // Renvoie templates/dashboard.html
    }
}