package com.example.sirh.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author moi
 * module 11
 * DemandeExplicationDto
 */
@Getter @Setter @NoArgsConstructor  @AllArgsConstructor @Builder 
public class DemandeExplicationDto {
    private Integer idDemande;
    private Integer idIncident;
    private String employeNom;
    private String matricule;
    private LocalDate dateEnvoi;
    private LocalDate dateLimiteReponse;
    private String motif;
    private String statutIncident;
    private boolean dejaRepondu;
}
