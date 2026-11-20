package com.example.sirh.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.sirh.model.DemandeConge;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CongeDashboardDto {

    private List<SoldeCongeView> soldes = new ArrayList<>();
    private List<DemandeConge> demandes = new ArrayList<>();
    private long demandesEnAttente;
    private long absencesEnCours;
}
