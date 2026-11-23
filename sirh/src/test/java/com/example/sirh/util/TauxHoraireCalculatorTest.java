package com.example.sirh.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class TauxHoraireCalculatorTest {

    @Test
    void calculeLesHeuresMensuellesDepuisLeRegime() {
        assertEquals(new BigDecimal("151.67"),
                TauxHoraireCalculator.heuresMensuelles(new BigDecimal("35")));
        assertEquals(new BigDecimal("173.33"),
                TauxHoraireCalculator.heuresMensuelles(new BigDecimal("40")));
    }

    @Test
    void retombeSurLaBaseLegaleSansRegime() {
        assertEquals(TauxHoraireCalculator.HEURES_MENSUELLES_LEGALES,
                TauxHoraireCalculator.heuresMensuelles(null));
        assertEquals(TauxHoraireCalculator.HEURES_MENSUELLES_LEGALES,
                TauxHoraireCalculator.heuresMensuelles(BigDecimal.ZERO));
    }

    @Test
    void calculeLeTauxHoraire() {
        assertEquals(new BigDecimal("13.1657"),
                TauxHoraireCalculator.tauxHoraire(
                        new BigDecimal("2000"), new BigDecimal("151.91")));
    }
}
