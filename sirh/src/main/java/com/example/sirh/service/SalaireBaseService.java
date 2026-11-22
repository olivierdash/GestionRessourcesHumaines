package com.example.sirh.service;

import com.example.sirh.model.Contrat;
import com.example.sirh.repository.ContratRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaireBaseService {

    private final ContratRepository contratRepository;

    @Transactional(readOnly = true)
    public BigDecimal calculerSalaireBaseMois(Integer idEmploye, LocalDate debut, LocalDate fin) {
        List<Contrat> contrats = contratRepository
                .findContratsCouvrantPeriode(idEmploye, debut, fin);

        if (contrats.isEmpty()) {
            throw new IllegalArgumentException(
                    "Aucun contrat couvrant cette période pour le salarié : " + idEmploye);
        }

        BigDecimal joursDuMois = BigDecimal.valueOf(debut.lengthOfMonth());
        BigDecimal total = BigDecimal.ZERO;

        for (Contrat c : contrats) {
            LocalDate debutReel = c.getDateDebut().isAfter(debut) ? c.getDateDebut() : debut;
            LocalDate finReelle = (c.getDateFin() == null || c.getDateFin().isAfter(fin))
                    ? fin
                    : c.getDateFin();

            long jours = ChronoUnit.DAYS.between(debutReel, finReelle) + 1;

            total = total.add(
                    c.getSalaireMensuel()
                            .multiply(BigDecimal.valueOf(jours))
                            .divide(joursDuMois, 4, RoundingMode.HALF_UP));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}