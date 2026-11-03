package com.example.sirh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/utilisateur")
public class UtilisateurController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "utilisateur/dashboard";
    }
}