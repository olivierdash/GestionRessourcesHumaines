package com.example.sirh.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder 
public class OffboardingStatusDto {
    private RuptureContratDto rupture;
    private boolean accesOk;
    private boolean materielOk;
    private boolean documentsOk;
    private boolean stcValide;
    private List<DesactivationAccesDto> acces;
    private List<MaterielDto> materiel;
    private List<DocumentSortieDto> documents;
}
