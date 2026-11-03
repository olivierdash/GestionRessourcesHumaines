package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "grille_salarial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrilleSalarial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grille")
    private Integer idGrille;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;
    @Column(name = "salaire_min", nullable = false)
    private BigDecimal salaireMin;
    @Column(name = "salaire_max", nullable = false)
    private BigDecimal salaireMax;
    @Column(name = "salaire_moyen", nullable = false)
    private BigDecimal salaireMoyen;
}