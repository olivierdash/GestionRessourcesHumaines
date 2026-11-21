package com.example.sirh.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.sirh.model.Utilisateur;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.DemandeCongeService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class CongeBadgeAdvice {

    private final CongeAccessService accessService;
    private final DemandeCongeService demandeCongeService;

    @ModelAttribute("congesEnAttenteCount")
    public long congesEnAttenteCount() {
        try {
            Utilisateur utilisateur = accessService.utilisateurCourantOptional().orElse(null);
            if (utilisateur == null) {
                return 0L;
            }
            return demandeCongeService.compterEnAttente(utilisateur);
        } catch (RuntimeException e) {
            return 0L;
        }
    }
}
