package com.example.sirh.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class TauxHoraireCalculator {

    public static final BigDecimal HEURES_MENSUELLES_LEGALES = new BigDecimal("151.67");

    private static final BigDecimal SEMAINES_PAR_MOIS =
            new BigDecimal("52").divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);

    private TauxHoraireCalculator() {
    }

    public static BigDecimal heuresMensuelles(BigDecimal heuresHebdomadaires) {
        if (heuresHebdomadaires == null || heuresHebdomadaires.signum() <= 0) {
            return HEURES_MENSUELLES_LEGALES;
        }
        return heuresHebdomadaires.multiply(SEMAINES_PAR_MOIS)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal tauxHoraire(BigDecimal salaireMensuel, BigDecimal heuresMensuelles) {
        if (salaireMensuel == null || salaireMensuel.signum() < 0
                || heuresMensuelles == null || heuresMensuelles.signum() <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return salaireMensuel.divide(heuresMensuelles, 4, RoundingMode.HALF_UP);
    }
}
