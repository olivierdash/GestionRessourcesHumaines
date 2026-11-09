package com.example.sirh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidaturePublicDto {

    private Integer id;
    private LocalDateTime dateDepot;

    // Statut
    private String statut;

    // Offre concernée
    private Integer offreId;
    private String offreTitre;
    private String poste;
    private String departement;
}