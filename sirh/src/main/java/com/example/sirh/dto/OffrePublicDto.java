package com.example.sirh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffrePublicDto {

    // Identité de l'offre
    private Integer id;
    private String titre;
    private String description;
    private String statut;

    // Poste associé
    private String intitulePoste;
    private String departement;
    private String lieu;
    private Integer dureeHebdo;

    // Demande de recrutement sous-jacente
    private Integer effectif;
    private LocalDate dateSouhaitee;
    private String motif;

    // Dates de l'offre
    private LocalDateTime datePublication;
    private LocalDateTime dateCloture;
}