package com.example.sirh.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PropositionPubliqueDto {
    private Integer idProposition;
    private String offreTitre;
    private String poste;
    private String departement;
    private BigDecimal salairePropose;
    private BigDecimal salaireFinal;
    private String statut;
    private LocalDateTime dateProposition;
    private LocalDateTime dateExpiration;
    private boolean attendReponse;
}