package com.example.sirh.service;

import com.example.sirh.model.JourFerie;
import com.example.sirh.repository.JourFerieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JourFerieService {

    private final JourFerieRepository jourFerieRepository;

    @Transactional(readOnly = true)
    public List<JourFerie> lister() {
        return jourFerieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LocalDate> listerJoursFeries(int annee) {
        List<LocalDate> result = new ArrayList<>();

        for (JourFerie jf : jourFerieRepository.findPourAnnee(annee)) {
            result.add(LocalDate.of(annee, jf.getMois(), jf.getJour()));
        }

        LocalDate paques = calculerPaques(annee);
        result.add(paques.plusDays(1));   // Lundi de Pâques
        result.add(paques.plusDays(39));  // Ascension
        result.add(paques.plusDays(50));  // Pentecôte

        return result.stream().sorted().toList();
    }

    @Transactional(readOnly = true)
    public boolean estFerie(LocalDate date) {
        if (!jourFerieRepository.findPourDate(
                date.getDayOfMonth(), date.getMonthValue(), date.getYear()).isEmpty()) {
            return true;
        }

        LocalDate paques = calculerPaques(date.getYear());
        return date.equals(paques.plusDays(1))
                || date.equals(paques.plusDays(39))
                || date.equals(paques.plusDays(50));
    }

    @Transactional(readOnly = true)
    public String libelleFerie(LocalDate date) {
        List<JourFerie> correspondants = jourFerieRepository.findPourDate(
                date.getDayOfMonth(), date.getMonthValue(), date.getYear());
        if (!correspondants.isEmpty()) {
            return correspondants.get(0).getLibelle();
        }

        LocalDate paques = calculerPaques(date.getYear());
        if (date.equals(paques.plusDays(1)))  return "Lundi de Pâques";
        if (date.equals(paques.plusDays(39))) return "Ascension";
        if (date.equals(paques.plusDays(50))) return "Pentecôte";

        return null;
    }

    private LocalDate calculerPaques(int annee) {
        int a = annee % 19;
        int b = annee / 100;
        int c = annee % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mois = (h + l - 7 * m + 114) / 31;
        int jour = ((h + l - 7 * m + 114) % 31) + 1;

        return LocalDate.of(annee, mois, jour);
    }

    @Transactional
    public JourFerie enregistrer(JourFerie form) {
        if (form.getJour() == null || form.getJour() < 1 || form.getJour() > 31) {
            throw new IllegalArgumentException("Le jour doit être compris entre 1 et 31.");
        }
        if (form.getMois() == null || form.getMois() < 1 || form.getMois() > 12) {
            throw new IllegalArgumentException("Le mois doit être compris entre 1 et 12.");
        }
        if (form.getLibelle() == null || form.getLibelle().isBlank()) {
            throw new IllegalArgumentException("Le libellé est obligatoire.");
        }
        if (form.getLibelle().trim().length() > 25) {
            throw new IllegalArgumentException("Le libellé ne peut pas dépasser 25 caractères.");
        }

        JourFerie jourFerie = form.getId() == null
                ? new JourFerie()
                : jourFerieRepository.findById(form.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Jour férié introuvable."));

        jourFerie.setJour(form.getJour());
        jourFerie.setMois(form.getMois());
        jourFerie.setAnnee(form.getAnnee());
        jourFerie.setLibelle(form.getLibelle().trim());

        return jourFerieRepository.save(jourFerie);
    }

    @Transactional
    public void supprimer(Integer id) {
        if (!jourFerieRepository.existsById(id)) {
            throw new IllegalArgumentException("Jour férié introuvable : " + id);
        }
        jourFerieRepository.deleteById(id);
    }
}