package com.example.sirh.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author moi
 * module 12
 * DesactivationAccesDto
 */

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder 
public class DesactivationAccesDto {
    private Integer id;
    private Integer idRupture;         // juste l'ID, pas l'objet
    private String typeAcces;          // BADGE, COMPTE_APPLICATIF, ...
    private String identifiant;
    private LocalDate dateDesactivation;
    private String statut;             // A_FAIRE, FAIT, ECHEC
    private String effectueParNom;     // "Alice RH" ou null
    private String commentaire;
}
