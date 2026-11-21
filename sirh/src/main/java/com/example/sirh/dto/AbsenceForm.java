package com.example.sirh.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbsenceForm {

    private Integer idEmploye;
    private Integer idTypeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motif;
}
