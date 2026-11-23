package com.example.sirh.dto;

import java.math.BigDecimal;

import com.example.sirh.model.enums.CodeVariablePaie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneRemunerationDto {

    private CodeVariablePaie code;
    private String libelle;
    private BigDecimal base;
    private BigDecimal taux;
    private BigDecimal quantite;
    private String unite;
    private BigDecimal montant;
}
