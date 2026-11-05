package com.example.sirh.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingStatutCountDto {

    private Integer idOnboarding;
    private String statutLibelle;
    private Long count;
}