package com.example.sirh.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "onboarding_modele_tache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeleTacheOnboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tache_modele")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_modele", nullable = false)
    private ModeleOnboarding modele;

    @Column(name = "titre", nullable = false, length = 255)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_role_responsable", nullable = false)
    private Role roleResponsable;

    @Column(name = "delai_jours", nullable = false)
    private Integer delaiJours;

    @Column(name = "est_obligatoire", nullable = false)
    @Builder.Default
    private Boolean obligatoire = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_document_requis")
    private TypeDocument typeDocumentRequis;

    @Column(name = "ordre", nullable = false)
    @Builder.Default
    private Integer ordre = 1;
}
