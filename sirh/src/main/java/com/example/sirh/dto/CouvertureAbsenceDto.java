package com.example.sirh.dto;

import java.time.LocalDate;

import com.example.sirh.model.enums.CategorieTypeConge;
import com.example.sirh.model.enums.StatutAbsence;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouvertureAbsenceDto {

    private Integer absenceId;
    private Integer demandeId;
    private String libelle;
    private CategorieTypeConge categorie;
    private StatutAbsence statut;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean justifiee;
}
