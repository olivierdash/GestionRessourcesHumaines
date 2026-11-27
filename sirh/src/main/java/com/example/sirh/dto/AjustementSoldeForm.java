package com.example.sirh.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AjustementSoldeForm {

    private BigDecimal jours;
    private String motif;
    private boolean forcer;
}
