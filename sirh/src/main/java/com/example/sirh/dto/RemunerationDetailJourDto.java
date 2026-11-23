package com.example.sirh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

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
public class RemunerationDetailJourDto {

    private LocalDate date;
    private String jour;
    private String statut;
    private String libelle;
    private BigDecimal heuresPrevues;
    private BigDecimal heuresTravaillees;
    private BigDecimal heuresSupplementaires;
    private BigDecimal retenue;
    private BigDecimal montantHeuresSupplementaires;
}
