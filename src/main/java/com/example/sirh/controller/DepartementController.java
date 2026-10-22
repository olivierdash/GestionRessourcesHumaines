package com.example.sirh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/departements")
public class DepartementController {

    /** GET /departements - Liste (Index). */
    @GetMapping
    public String index(Model model) {
        return null;
    }

    /** GET /departements/create - Formulaire de création. */
    @GetMapping("/create")
    public String create(Model model) {
        return null;
    }

    /** POST /departements - Enregistrement (Création). */
    @PostMapping
    public String store(
            @RequestParam String titre,
            @RequestParam(required = false) Integer idManager) {
        return null;
    }

    /** GET /departements/{id} - Fiche détail (Optionnel pour l'instant). */
    @GetMapping("/{id}")
    public String show(@PathVariable Integer id, Model model) {
        return null;
    }

    /** GET /departements/{id}/edit - Formulaire de modification. */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        return null;
    }

    /** PUT /departements/{id} - Remplacement complet (Mise à jour). */
    @PutMapping("/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String titre,
            @RequestParam(required = false) Integer idManager) {
        return null;
    }
}