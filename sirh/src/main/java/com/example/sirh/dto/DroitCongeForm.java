package com.example.sirh.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DroitCongeForm {

    private Integer id;
    private Integer idTypeConge;
    private Integer idTypeContrat;
    private Integer ancienneteMinMois = 0;
    private Integer ancienneteMaxMois;
    private String modeAcquisition = "ANNUEL";
    private java.math.BigDecimal joursParAn;
    private Boolean prorataAnneeEmbauche = true;
    private Boolean actif = true;
    private String description;
}
