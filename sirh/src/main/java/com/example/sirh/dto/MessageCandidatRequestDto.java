package com.example.sirh.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MessageCandidatRequestDto {
    private Integer idCandidat;
    private String contenu;
    private BigDecimal salairePropose;
}