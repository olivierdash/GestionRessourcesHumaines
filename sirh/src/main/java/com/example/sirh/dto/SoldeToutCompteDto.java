package com.example.sirh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoldeToutCompteDto {
    private Integer idStc;
    private Integer idRupture;
    private LocalDate dateCalcul;
    private BigDecimal salaireMensuelReference;
    private BigDecimal joursCongesRestants;
    private BigDecimal totalGains;
    private BigDecimal totalRetenues;
    private BigDecimal netAPayer;
    private String statut;
    private LocalDate dateValidation;
}
