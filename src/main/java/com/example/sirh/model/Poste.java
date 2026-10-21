package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "poste")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Poste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_poste")
    private Integer idPoste;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;

    @Column(name = "date_effet")
    private LocalDate dateEffet;

    @Column(name = "intitule", nullable = false)
    private String intitule;

    @Column(name = "lieu")
    private String lieu;

    @Column(name = "duree_hebdo")
    private Integer dureeHebdo;
}