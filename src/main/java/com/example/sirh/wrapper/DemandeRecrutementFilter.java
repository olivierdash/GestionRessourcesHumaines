package com.example.sirh.wrapper;

import java.time.LocalDate;

import lombok.Data;

@Data 
public class DemandeRecrutementFilter {

    private LocalDate dateMin;
    private LocalDate dateMax;
    private Integer idPoste;
    private Integer effectifMin;
    private Integer effectifMax;
    private Integer idManager;
    private Integer idDepartement;
    private Integer idMotifDemande;      // <-- au lieu de MotifRecrutement / idMotif
}