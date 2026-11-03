package com.example.sirh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeResumeDto {
    private Integer id;
    private String reference; // ex: "REC-2024-001" ou "#" + id
    private String poste;
    private String departement;
    private LocalDateTime dateCreation;
    private String statut;
}