package com.example.sirh.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.dto.TypeCongeForm;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.TypeConge;
import com.example.sirh.model.enums.CategorieTypeConge;
import com.example.sirh.model.enums.ModeDecompteConge;
import com.example.sirh.repository.TypeCongeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TypeCongeService {

    private final TypeCongeRepository typeCongeRepository;

    @Transactional(readOnly = true)
    public List<TypeConge> lister() {
        return typeCongeRepository.findAllByOrderByOrdreAffichageAscLibelleAsc();
    }

    @Transactional(readOnly = true)
    public List<TypeConge> listerActifs() {
        return typeCongeRepository.findByActifTrueOrderByOrdreAffichageAscLibelleAsc();
    }

    @Transactional(readOnly = true)
    public List<TypeConge> listerActifsPourEmploye() {
        return typeCongeRepository
                .findByCategorieAndActifTrueOrderByOrdreAffichageAsc(CategorieTypeConge.CONGE)
                .stream()
                .filter(t -> Boolean.TRUE.equals(t.getDemandeEmployeAutorisee()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TypeConge> listerAbsencesActives() {
        return typeCongeRepository
                .findByCategorieAndActifTrueOrderByOrdreAffichageAsc(CategorieTypeConge.ABSENCE);
    }

    @Transactional(readOnly = true)
    public List<TypeConge> listerCongesAvecSoldeActifs() {
        return typeCongeRepository
                .findByCategorieAndConsommeSoldeTrueAndActifTrueOrderByOrdreAffichageAsc(
                        CategorieTypeConge.CONGE);
    }

    @Transactional(readOnly = true)
    public TypeConge getById(Integer id) {
        return typeCongeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Type de congé introuvable : " + id));
    }

    @Transactional
    public TypeConge enregistrer(TypeCongeForm form) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (form.getCode() == null || form.getCode().isBlank()) {
            errors.put("code", "Le code est obligatoire.");
        }
        if (form.getLibelle() == null || form.getLibelle().isBlank()) {
            errors.put("libelle", "Le libellé est obligatoire.");
        }
        if (form.getCategorie() == null || form.getCategorie().isBlank()) {
            errors.put("categorie", "La catégorie est obligatoire.");
        }
        if (form.getModeDecompte() == null || form.getModeDecompte().isBlank()) {
            errors.put("modeDecompte", "Le mode de décompte est obligatoire.");
        }
        if (form.getDelaiJustificatifJours() != null && form.getDelaiJustificatifJours() < 0) {
            errors.put("delaiJustificatifJours", "Le délai doit être positif ou nul.");
        }
        if (form.getDureeMaxJours() != null && form.getDureeMaxJours().signum() <= 0) {
            errors.put("dureeMaxJours", "La durée maximale doit être positive.");
        }
        if (form.getPlafondReportJours() != null && form.getPlafondReportJours().signum() < 0) {
            errors.put("plafondReportJours", "Le plafond de report doit être positif ou nul.");
        }

        CategorieTypeConge categorie = null;
        ModeDecompteConge mode = null;
        try {
            if (form.getCategorie() != null && !form.getCategorie().isBlank()) {
                categorie = CategorieTypeConge.valueOf(form.getCategorie());
            }
        } catch (IllegalArgumentException e) {
            errors.put("categorie", "Catégorie invalide.");
        }
        try {
            if (form.getModeDecompte() != null && !form.getModeDecompte().isBlank()) {
                mode = ModeDecompteConge.valueOf(form.getModeDecompte());
            }
        } catch (IllegalArgumentException e) {
            errors.put("modeDecompte", "Mode de décompte invalide.");
        }

        Boolean consommeSolde = form.getConsommeSolde();
        Boolean demandeEmployeAutorisee = form.getDemandeEmployeAutorisee();
        Boolean reportAutorise = form.getReportAutorise();
        BigDecimal plafondReport = form.getPlafondReportJours();
        Integer delai = form.getDelaiJustificatifJours();

        if (categorie == CategorieTypeConge.ABSENCE) {
            consommeSolde = false;
            demandeEmployeAutorisee = false;
            reportAutorise = false;
            plafondReport = null;
        }
        if (!Boolean.TRUE.equals(form.getJustificatifRequis())) {
            delai = null;
        }

        String code = form.getCode() == null ? null : form.getCode().trim().toUpperCase();
        if (code != null && !code.isBlank()) {
            typeCongeRepository.findByCodeIgnoreCase(code).ifPresent(existant -> {
                if (form.getId() == null || !existant.getId().equals(form.getId())) {
                    errors.put("code", "Ce code existe déjà.");
                }
            });
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        TypeConge cible = form.getId() == null ? new TypeConge() : getById(form.getId());

        cible.setCode(code);
        cible.setLibelle(form.getLibelle().trim());
        cible.setDescription(form.getDescription());
        cible.setCategorie(categorie);
        cible.setModeDecompte(mode);
        cible.setPaye(Boolean.TRUE.equals(form.getPaye()));
        cible.setConsommeSolde(Boolean.TRUE.equals(consommeSolde));
        cible.setJustificatifRequis(Boolean.TRUE.equals(form.getJustificatifRequis()));
        cible.setDelaiJustificatifJours(delai);
        cible.setDureeMaxJours(form.getDureeMaxJours());
        cible.setReportAutorise(Boolean.TRUE.equals(reportAutorise));
        cible.setPlafondReportJours(plafondReport);
        cible.setDemandeEmployeAutorisee(Boolean.TRUE.equals(demandeEmployeAutorisee));
        cible.setActif(form.getActif() == null || form.getActif());
        cible.setOrdreAffichage(form.getOrdreAffichage() == null ? 0 : form.getOrdreAffichage());
        cible.setCouleur(form.getCouleur());

        return typeCongeRepository.save(cible);
    }

    @Transactional
    public void basculerActif(Integer id) {
        TypeConge type = getById(id);
        type.setActif(!Boolean.TRUE.equals(type.getActif()));
        typeCongeRepository.save(type);
    }

    public BigDecimal plafondReport(TypeConge type) {
        return type.getPlafondReportJours() == null
                ? new BigDecimal("9999")
                : type.getPlafondReportJours();
    }
}
