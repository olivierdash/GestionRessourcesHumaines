package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coordonnees_employe")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CoordonneesEmploye {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_coordonnees")
    private Integer idCoordonnees;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false, unique = true)
    private Employe employe;

    private String adresse;
    
    @Column(name = "code_postal")
    private String codePostal;
    
    private String ville;
    private String pays;
    
    @Column(name = "situation_matrimoniale")
    private String situationMatrimoniale;
    
    @Column(name = "nombre_enfants")
    private Integer nombreEnfants;
    
    @Column(name = "telephone_personnel")
    private String telephonePersonnel;
}