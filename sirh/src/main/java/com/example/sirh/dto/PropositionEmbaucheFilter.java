package com.example.sirh.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class PropositionEmbaucheFilter {
    private Integer statut = null;
    private Integer idCandidat;
    private Integer idPoste;
    private Integer idDepartement;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateExpirationMax;

    private boolean attendReponse;
}