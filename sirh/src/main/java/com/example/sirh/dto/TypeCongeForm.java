package com.example.sirh.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypeCongeForm {

    private Integer id;
    private String code;
    private String libelle;
    private String description;
    private String categorie;
    private String modeDecompte;
    private Boolean paye = true;
    private Boolean consommeSolde = true;
    private Boolean justificatifRequis = false;
    private Integer delaiJustificatifJours;
    private BigDecimal dureeMaxJours;
    private Boolean reportAutorise = false;
    private BigDecimal plafondReportJours;
    private Boolean demandeEmployeAutorisee = true;
    private Boolean actif = true;
    private Integer ordreAffichage = 0;
    private String couleur;
}
