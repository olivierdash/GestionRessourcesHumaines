package com.example.sirh.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "declenchement_besoin_formation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeclenchementBesoinFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_declenchement")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_besoin_formation", nullable = false)
    private BesoinFormation besoinFormation;

    @Column(nullable = false, length = 30)
    private String motif;

    @Column(length = 500)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @CreationTimestamp
    @Column(name = "date_declenchement", nullable = false, updatable = false)
    private LocalDateTime dateDeclenchement;
}
