package com.example.sirh.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class LoginChangeDTO {
    private Integer idUtilisateur;
    private String nouveauMotDePasse;
    private String confirmationMotDePasse;
    private String codeSecret;
    private Integer idAdminConnecte;
}