package com.example.sirh.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class EmployeFilter {
    private Integer idDepartement;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateMin;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateMax;
    
    private Integer statusEmployer = null; 
}