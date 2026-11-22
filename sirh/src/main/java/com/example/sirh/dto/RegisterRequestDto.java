package com.example.sirh.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterRequestDto extends LoginRequestDto {
    private String prenom;
    private String nom;
}