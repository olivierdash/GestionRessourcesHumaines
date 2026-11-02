package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity 
@Table(name = "motif_demande")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MotifDemande {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_motif_demande")
    private Integer idMotifDemande;

    @Column(name = "libelle", nullable = false, unique = true, length = 100)
    private String libelle;
}