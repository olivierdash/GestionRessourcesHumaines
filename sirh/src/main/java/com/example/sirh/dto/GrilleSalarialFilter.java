package com.example.sirh.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GrilleSalarialFilter {
    private Integer idDepartement;
    private BigDecimal salaireMin;
    private BigDecimal salaireMax;
}