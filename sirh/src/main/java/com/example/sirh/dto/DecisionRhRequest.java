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
 * DecisionRhRequest
 */
@Setter @Getter @NoArgsConstructor @AllArgsConstructor @Builder 
public class DecisionRhRequest {
    private String typeSanction;       // AUCUNE, AVERTISSEMENT, BLAME, MISE_A_PIED, LICENCIEMENT
    private LocalDate dateNotification;
    private String commentaireDecision;
}
