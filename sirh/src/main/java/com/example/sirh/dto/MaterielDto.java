package com.example.sirh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author
 * module 12
 * MaterielDto
 */

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder 
public class MaterielDto {
    private Integer id;
    private Integer idEmployer;
    private String designation;
    private String numeroSerie;
    private LocalDate dateRemise;
    private LocalDate dateRestitution;   // null = pas encore rendu
    private String etatRestitution;      // BON, ENDOMMAGE, PERDU ou null
    private BigDecimal montantRetenue;
    private Boolean restitue;            // raccourci calculé côté serveur
}
