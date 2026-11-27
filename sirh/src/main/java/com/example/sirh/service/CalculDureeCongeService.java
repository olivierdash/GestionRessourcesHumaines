package com.example.sirh.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.sirh.dto.DureeCongeDto;
import com.example.sirh.model.TypeConge;
import com.example.sirh.model.enums.DemiJournee;
import com.example.sirh.model.enums.ModeDecompteConge;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalculDureeCongeService {

    private static final BigDecimal DEMI = new BigDecimal("0.5");

    private final JourFerieService jourFerieService;

    public BigDecimal calculer(TypeConge type, LocalDate debut, LocalDate fin) {
        return detail(type, debut, fin, null, null).getNombreJours();
    }

    public DureeCongeDto detail(TypeConge type, LocalDate debut, LocalDate fin,
                                DemiJournee demiDebut, DemiJournee demiFin) {
        if (type == null) {
            throw new IllegalArgumentException("Le type de congé est obligatoire.");
        }
        if (debut == null || fin == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires.");
        }
        if (fin.isBefore(debut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas précéder la date de début.");
        }

        Map<LocalDate, BigDecimal> jours = new LinkedHashMap<>();

        for (LocalDate date = debut; !date.isAfter(fin); date = date.plusDays(1)) {
            BigDecimal poids = poidsDuJour(type, date);
            if (poids.signum() == 0) {
                continue;
            }
            if (date.equals(debut) && demiDebut == DemiJournee.APRES_MIDI) {
                poids = poids.subtract(DEMI);
            }
            if (date.equals(fin) && demiFin == DemiJournee.MATIN) {
                poids = poids.subtract(DEMI);
            }
            if (poids.signum() > 0) {
                jours.put(date, poids);
            }
        }

        DureeCongeDto resultat = new DureeCongeDto();
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, BigDecimal> parAnnee = new LinkedHashMap<>();
        Map<String, BigDecimal> parMois = new LinkedHashMap<>();

        for (Map.Entry<LocalDate, BigDecimal> entree : jours.entrySet()) {
            LocalDate date = entree.getKey();
            BigDecimal poids = entree.getValue();
            total = total.add(poids);
            parAnnee.merge(date.getYear(), poids, BigDecimal::add);
            parMois.merge(YearMonth.from(date).toString(), poids, BigDecimal::add);
        }

        total = total.setScale(2, RoundingMode.HALF_UP);
        if (total.signum() <= 0) {
            throw new IllegalArgumentException(
                    "La période ne contient aucun jour décomptable pour ce type de congé.");
        }

        resultat.setNombreJours(total);
        resultat.setParAnnee(parAnnee);
        resultat.setParMois(parMois);
        return resultat;
    }

    public Map<Integer, BigDecimal> repartirParAnnee(TypeConge type, LocalDate debut, LocalDate fin) {
        return detail(type, debut, fin, null, null).getParAnnee();
    }

    public Map<String, BigDecimal> repartirParMois(TypeConge type, LocalDate debut, LocalDate fin) {
        return detail(type, debut, fin, null, null).getParMois();
    }

    private BigDecimal poidsDuJour(TypeConge type, LocalDate date) {
        if (type.getModeDecompte() == ModeDecompteConge.CALENDAIRES) {
            return BigDecimal.ONE;
        }
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return BigDecimal.ZERO;
        }
        if (jourFerieService.estFerie(date)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.ONE;
    }
}
