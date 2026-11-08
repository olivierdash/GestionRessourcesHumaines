package com.example.sirh.controller;

import com.example.sirh.config.CustomUserDetails;
import com.example.sirh.service.TypeContratService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/types-contrat")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RH')")
public class TypeContratController {

    private final TypeContratService typeContratService;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("types", typeContratService.getAllTypes());
        return "contrats/type-contrat/index";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("type", typeContratService.getById(id));
        model.addAttribute("historique", typeContratService.getHistorique(id));
        return "contrats/type-contrat/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam(required = false) Integer dureeMaxMois,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes ra) {
        try {
            typeContratService.modifierDureeMax(id, dureeMaxMois,
                    userDetails.getUtilisateur().getIdUtilisateur());
            ra.addFlashAttribute("succes", "Durée maximale mise à jour.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/types-contrat/" + id + "/edit";
    }
}