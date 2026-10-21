package com.example.sirh.dto;

import java.time.LocalDateTime;

/**
 * Projection (View) regroupant demande_recrutement x utilisateur x poste x departement
 */
public record DemandeRecrutementLib(
        Integer idDemandeRecrutement,
        LocalDateTime dateCreation,
        Integer effectif,
        String motif,
        String nomManager,
        String intitulePoste,
        String titreDepartement
) {
}