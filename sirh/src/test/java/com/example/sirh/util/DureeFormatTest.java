package com.example.sirh.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class DureeFormatTest {

    @Test
    void formater_journeeComplete() {
        assertEquals("1 j", DureeFormat.jours(new BigDecimal("1.00")));
    }

    @Test
    void formater_demiJourneeEnHeures() {
        assertEquals("4 h", DureeFormat.jours(new BigDecimal("0.50")));
    }

    @Test
    void formater_joursEtHeures() {
        assertEquals("1 j 4 h", DureeFormat.jours(new BigDecimal("1.50")));
    }

    @Test
    void formater_plusieursJours() {
        assertEquals("12 j", DureeFormat.jours(new BigDecimal("12.00")));
    }

    @Test
    void formater_valeurNegativeAvecSigne() {
        assertEquals("-4 h", DureeFormat.joursSigne(new BigDecimal("-0.50")));
        assertEquals("+1 j 4 h", DureeFormat.joursSigne(new BigDecimal("1.50")));
    }

    @Test
    void formater_valeurNulle() {
        assertEquals("0 j", DureeFormat.jours(BigDecimal.ZERO));
        assertEquals("0 j", DureeFormat.jours(null));
    }
}
