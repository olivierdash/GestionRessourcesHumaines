package com.example.sirh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatPublicDto {
    private Integer id;
    private String prenom;
    private String nom;
    private String email;
}