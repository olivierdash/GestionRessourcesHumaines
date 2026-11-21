package com.example.sirh.dto;

import com.example.sirh.model.enums.StatutVariablePaie;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariablePaieFilter {

    private String periode;
    private Integer employeId;
    private StatutVariablePaie statut;
}
