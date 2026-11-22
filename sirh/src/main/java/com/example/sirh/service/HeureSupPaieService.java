package com.example.sirh.service;

import com.example.sirh.model.PalierHeureSup;
import com.example.sirh.repository.PalierHeureSupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HeureSupPaieService {

    private final PalierHeureSupRepository palierRepository;

    @Transactional(readOnly = true)
    public BigDecimal calculerMontantSemaine(int minutesSup, BigDecimal tauxHoraire) {
        List<PalierHeureSup> paliers = palierRepository.findByActifTrueOrderByOrdreAsc();

        BigDecimal heuresRestantes = BigDecimal.valueOf(minutesSup)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal total = BigDecimal.ZERO;

        for (PalierHeureSup palier : paliers) {
            if (heuresRestantes.signum() <= 0) {
                break;
            }
            BigDecimal heures = (palier.getHeuresTranche() == null)
                    ? heuresRestantes
                    : heuresRestantes.min(palier.getHeuresTranche());

            total = total.add(
                    heures.multiply(tauxHoraire).multiply(palier.getCoefficient()));
            heuresRestantes = heuresRestantes.subtract(heures);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}