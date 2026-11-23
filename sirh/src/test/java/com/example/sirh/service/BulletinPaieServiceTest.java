package com.example.sirh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.sirh.dto.LigneRemunerationDto;
import com.example.sirh.dto.RemunerationDto;
import com.example.sirh.model.enums.CodeVariablePaie;

class BulletinPaieServiceTest {

    @Test
    void genereUnPdfValide() {
        RemunerationDto remuneration = RemunerationDto.builder()
                .employeId(1)
                .matricule("EMP001")
                .nomComplet("Jean Test")
                .periode(YearMonth.of(2026, 11))
                .dateDebut(LocalDate.of(2026, 11, 1))
                .dateFin(LocalDate.of(2026, 11, 30))
                .forfaitJours(false)
                .salaireMensuel(new BigDecimal("2500.00"))
                .heuresHebdomadaires(new BigDecimal("35"))
                .heuresMensuelles(new BigDecimal("151.67"))
                .tauxHoraire(new BigDecimal("16.4832"))
                .joursOuvresMois(new BigDecimal("21"))
                .salaireBase(new BigDecimal("2500.00"))
                .totalRetenues(new BigDecimal("120.00"))
                .totalHeuresSupplementaires(new BigDecimal("2"))
                .brut(new BigDecimal("2421.20"))
                .lignes(List.of(
                        LigneRemunerationDto.builder()
                                .code(CodeVariablePaie.BASE_CALCUL)
                                .libelle("Salaire de base")
                                .montant(new BigDecimal("2500.00"))
                                .build(),
                        LigneRemunerationDto.builder()
                                .code(CodeVariablePaie.BRUT_TOTAL)
                                .libelle("Brut total")
                                .montant(new BigDecimal("2421.20"))
                                .build()))
                .build();

        byte[] pdf = new BulletinPaieService().generer(remuneration);

        assertTrue(pdf.length > 100);
        assertEquals('%', pdf[0]);
        assertEquals('P', pdf[1]);
        assertEquals('D', pdf[2]);
        assertEquals('F', pdf[3]);
    }
}
