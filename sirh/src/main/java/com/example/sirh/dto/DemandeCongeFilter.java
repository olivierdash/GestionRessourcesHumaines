package com.example.sirh.dto;

import java.time.LocalDate;

import com.example.sirh.model.enums.StatutDemandeConge;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemandeCongeFilter {

    private Integer employeId;
    private Integer typeId;
    private StatutDemandeConge statut;
    private LocalDate debut;
    private LocalDate fin;

    public boolean isActif() {
        return employeId != null || typeId != null || statut != null || debut != null || fin != null;
    }
}
