package com.example.sirh.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import com.example.sirh.model.MotifDemande;

import java.time.LocalDate;

@Getter
@Setter
public class DemandeRecrutementDTO {

    private Integer idDemandeRecrutement;
    private Integer idManager;
    private Integer idPoste;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateSouhaitee;
    private Integer effectif;
    private MotifDemande motif; 
    private Integer idEtat;
    private String etat;
    private String remarque;

    public DemandeRecrutementDTO(){
        this.motif = new MotifDemande();
    }
}