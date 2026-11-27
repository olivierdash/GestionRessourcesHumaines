package com.example.sirh.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemandeCongeForm {

    private Integer idTypeConge;
    private Integer idEmploye;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motif;
}
