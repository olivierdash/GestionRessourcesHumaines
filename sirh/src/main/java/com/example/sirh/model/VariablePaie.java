package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "variable_paie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariablePaie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variable_paie")
    private Integer idVariablePaie;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employer", nullable = false)
    private Employe employe;
    @Column(name = "periode", nullable = false)
    private LocalDate periode;
    @Column(name = "type", nullable = false)
    private String type;
    @Column(name = "libelle")
    private String libelle;
    @Column(name = "valeur", nullable = false)
    private BigDecimal valeur;
}