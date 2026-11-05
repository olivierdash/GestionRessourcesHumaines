package com.example.sirh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RHDashboardDto {
    private long demandesAValider;
    private long contratsARenouvelerSous30j;
    private long onboardingsEnCours;
    private double onboardingsProgressionMoyenne; // en %
    private List<FunnelStepDto> funnel;
    private Double timeToFillMoyen; // null si aucune embauche dans la période
    private List<ChartPointDto> chartData;
    private String period;       // "7d", "30d", "trimestre", "annee"
    private String periodLabel;  // "7 derniers jours", "Ce trimestre"…
}