package com.example.sirh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

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
public class RemunerationDto {

    private Integer employeId;
    private String matricule;
    private String nomComplet;
    private YearMonth periode;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private boolean forfaitJours;
    private BigDecimal salaireMensuel;
    private BigDecimal heuresHebdomadaires;
    private BigDecimal heuresMensuelles;
    private BigDecimal tauxHoraire;
    private BigDecimal joursOuvresMois;
    private BigDecimal salaireBase;
    private BigDecimal totalRetenues;
    private BigDecimal totalHeuresSupplementaires;
    private BigDecimal brut;

    @Builder.Default
    private List<LigneRemunerationDto> lignes = new ArrayList<>();

    @Builder.Default
    private List<RemunerationDetailJourDto> jours = new ArrayList<>();
}
