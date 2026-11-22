package com.example.sirh.dto;

import lombok.Data;

@Data
public class CandidatLoginChangeDTO {
    private String email;
    private String nouveauMotDePasse;
    private String confirmationMotDePasse;
}
