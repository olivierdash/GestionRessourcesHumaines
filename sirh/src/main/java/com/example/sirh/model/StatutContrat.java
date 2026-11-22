package com.example.sirh.model;

import jakarta.persistence.*;

@Entity
@Table(name = "statut_contrat")
public class StatutContrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_contrat")
    private Integer id;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}