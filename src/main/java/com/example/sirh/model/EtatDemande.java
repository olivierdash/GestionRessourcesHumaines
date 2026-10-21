package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "etat_demande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtatDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etat_demande")
    private Integer idEtatDemande;

    /** 'BROUILLON', 'VALIDE_RH', 'VALIDE_FINANCE', 'VALIDE_DG' */
    @Column(name = "intitule", nullable = false, length = 100)
    private String intitule;
}
