package com.example.sirh.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.sirh.util.DureeFormat;

/**
 * Expose le formatage des durées aux templates Thymeleaf via {@code @formatDuree}.
 */
@Service("formatDuree")
public class DureeFormatService {

    public String jours(BigDecimal valeur) {
        return DureeFormat.jours(valeur);
    }

    public String joursSigne(BigDecimal valeur) {
        return DureeFormat.joursSigne(valeur);
    }
}
