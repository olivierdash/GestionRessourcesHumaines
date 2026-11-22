package com.example.sirh.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PropositionDetailPubliqueDto {
    private Integer idProposition;
    private String offreTitre;
    private String poste;
    private String departement;
    private BigDecimal salairePropose;
    private String statut;
    private LocalDateTime dateProposition;
    private LocalDateTime dateExpiration;
    private LocalDateTime dateDecision;
    private String remarque;
    private List<MessagePubliqueDto> messages;
    private boolean actionRequise;
}