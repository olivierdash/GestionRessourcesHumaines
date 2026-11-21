package com.example.sirh.dto;

import java.time.LocalDate;

import com.example.sirh.model.enums.StatutAbsence;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbsenceFilter {

    private Integer employeId;
    private Integer typeId;
    private StatutAbsence statut;
    private LocalDate debut;
    private LocalDate fin;

    public boolean isActif() {
        return employeId != null || typeId != null || statut != null || debut != null || fin != null;
    }
}
