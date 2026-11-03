package com.example.sirh.dto;

import com.example.sirh.model.MotifDemande;
import java.time.LocalDateTime;

public record DemandeRecrutementLib(
    Integer idDemandeRecrutement,
    LocalDateTime dateCreation,
    Integer effectif,
    MotifDemande motif,          // <-- entité directement
    String managerNom,
    String posteIntitule,
    String departementTitre
) {}