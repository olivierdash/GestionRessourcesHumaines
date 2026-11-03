package com.example.sirh.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "statut_demande_recrutement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatutDemandeRecrutement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_demande_recrutement")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String libelle;
}
