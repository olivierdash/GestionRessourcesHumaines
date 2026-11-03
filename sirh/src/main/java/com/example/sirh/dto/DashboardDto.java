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
public class DashboardDto {
    private long demandesEnCours;
    private long postesOuverts;
    private long candidaturesSemaine;
    private List<ChartPointDto> chartData;
    private List<DemandeResumeDto> dernieresDemandes;
}