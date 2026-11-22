package com.example.sirh.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author moi
 * module 11
 * IncidentDto
 */

@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder 
public class IncidentDto {
    private Integer idIncident;
    private Integer idEmployer;
    private String employeNom;
    private String matricule;
    private LocalDate dateFait;
    private String descriptions;
    private String gravite;
    private String statutInstruction;
    private String declarantNom;
}
