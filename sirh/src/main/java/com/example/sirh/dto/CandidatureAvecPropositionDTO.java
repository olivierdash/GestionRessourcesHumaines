package com.example.sirh.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CandidatureAvecPropositionDTO {

    private final Integer idCandidature;
    private final String prenomCandidat;
    private final String nomCandidat;
    private final LocalDateTime dateDepot;
    private final Integer idStatutCandidature;
    private final String statutCandidatureLibelle;

    private final Integer idProposition;
    private final String statutPropositionLibelle;
    private final BigDecimal salairePropose;
}