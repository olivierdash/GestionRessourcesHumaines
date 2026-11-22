package com.example.sirh.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author moi
 * module 12
 * DocumentSortieDto
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentSortieDto {
    private Integer id;
    private Integer idRupture;
    private String typeDocument;         // SOLDE_TOUT_COMPTE, CERTIFICAT_TRAVAIL, ...
    private String reference;
    private String fichierUrl;
    private LocalDate dateGeneration;
    private LocalDate dateRemise;        // null = pas encore remis
    private String genereParNom;
    private Boolean remis;               // raccourci calculé côté serveur
}
