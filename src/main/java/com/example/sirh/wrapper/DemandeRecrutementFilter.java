package com.example.sirh.wrapper;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DemandeRecrutementFilter {
    private LocalDate dateMin;
    private LocalDate dateMax;
    
    private Integer idPoste;
    private Integer effectifMin;
    private Integer effectifMax;
    private Integer idManager;
    private Integer idDepartement;
}