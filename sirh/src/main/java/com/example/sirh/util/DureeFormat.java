package com.example.sirh.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Formate une durée exprimée en jours en jours et heures, sans décimale.
 * Une journée de travail est considérée comme 8 heures.
 */
public final class DureeFormat {

    public static final BigDecimal HEURES_PAR_JOUR = new BigDecimal("8");

    private static final BigDecimal MINUTES_PAR_HEURE = new BigDecimal("60");

    private DureeFormat() {
    }

    public static String jours(BigDecimal valeur) {
        return formater(valeur, false);
    }

    public static String joursSigne(BigDecimal valeur) {
        return formater(valeur, true);
    }

    private static String formater(BigDecimal valeur, boolean signePositif) {
        if (valeur == null) {
            return "0 j";
        }

        BigDecimal arrondi = valeur.setScale(2, RoundingMode.HALF_UP);
        if (arrondi.signum() == 0) {
            return "0 j";
        }
        String prefixe = arrondi.signum() < 0 ? "-" : (signePositif ? "+" : "");
        BigDecimal absolu = arrondi.abs();

        BigDecimal jours = absolu.divideToIntegralValue(BigDecimal.ONE);
        BigDecimal minutes = absolu.subtract(jours)
                .multiply(HEURES_PAR_JOUR)
                .multiply(MINUTES_PAR_HEURE)
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal heures = minutes.divideToIntegralValue(MINUTES_PAR_HEURE);
        BigDecimal minutesRestantes = minutes.subtract(
                heures.multiply(MINUTES_PAR_HEURE));

        List<String> parties = new ArrayList<>();
        if (jours.signum() > 0) {
            parties.add(entier(jours) + " j");
        }
        if (heures.signum() > 0 || (jours.signum() > 0 && minutesRestantes.signum() > 0)) {
            parties.add(entier(heures) + " h");
        }
        if (minutesRestantes.signum() > 0) {
            parties.add(entier(minutesRestantes) + " min");
        }

        if (parties.isEmpty()) {
            return "0 j";
        }
        return prefixe + String.join(" ", parties);
    }

    private static String entier(BigDecimal valeur) {
        return valeur.stripTrailingZeros().toPlainString();
    }
}
