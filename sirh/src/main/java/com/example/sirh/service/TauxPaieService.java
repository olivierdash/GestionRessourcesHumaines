package com.example.sirh.service;

import com.example.sirh.constants.ConstantSirh;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class TauxPaieService {

    public BigDecimal tauxHoraire(BigDecimal salaireMensuel) {
        return salaireMensuel.divide(ConstantSirh.Paie.HEURES_PAR_MOIS, 4, RoundingMode.HALF_UP);
    }

    public BigDecimal tauxJournalier(BigDecimal salaireMensuel) {
        return salaireMensuel.divide(ConstantSirh.Paie.JOURS_PAR_MOIS, 4, RoundingMode.HALF_UP);
    }
}