package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "etat_demande")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EtatDemande {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etat_demande")
    private Integer idEtatDemande;
    @Column(nullable = false, unique = true)
    private String intitule;
}
