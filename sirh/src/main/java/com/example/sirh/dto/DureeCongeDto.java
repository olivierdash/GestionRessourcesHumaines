package com.example.sirh.dto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DureeCongeDto {

    private BigDecimal nombreJours = BigDecimal.ZERO;
    private Map<Integer, BigDecimal> parAnnee = new LinkedHashMap<>();
    private Map<String, BigDecimal> parMois = new LinkedHashMap<>();
}
