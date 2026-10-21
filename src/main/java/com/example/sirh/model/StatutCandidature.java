package com.example.sirh.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "statut_candidature")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatutCandidature {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_candidature")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String libelle;
}
