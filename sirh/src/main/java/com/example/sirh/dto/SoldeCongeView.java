package com.example.sirh.dto;

import java.math.BigDecimal;

import com.example.sirh.model.SoldeConge;
import com.example.sirh.model.TypeConge;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SoldeCongeView {

    private TypeConge type;
    private SoldeConge solde;
    private BigDecimal disponible = BigDecimal.ZERO;

    public boolean isInitialise() {
        return solde != null;
    }
}
