package com.example.sirh.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author moi
 * Module 12
 * RuptureContratDto
 */
@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder 
public class RuptureContratDto {
    private Integer idRupture;
    private Integer idEmployer;
    private String employeNom;
    private String matricule;
    private String motifRupture;
    private String statutRupture;
    private LocalDate dateNotification;
    private Integer dureePreavisJours;
    private LocalDate dateSortie;
    private String commentaire;
    private Boolean archive;
}
