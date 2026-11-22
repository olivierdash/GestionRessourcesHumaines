package com.example.sirh.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class MessagePubliqueDto {
    private Integer id;
    private String auteur;
    private boolean deCandidat;
    private String contenu;
    private BigDecimal salairePropose;
    private LocalDateTime dateCreation;
}