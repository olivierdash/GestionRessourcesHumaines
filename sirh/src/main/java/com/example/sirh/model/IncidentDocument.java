package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
/**
 * @author moi
 * module 11
 * IncidentDocument
 */

@Entity
@Table(name = "incident_document")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IncidentDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incident_document")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_incident", nullable = false)
    private Incident incident;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "fichier_url", nullable = false, length = 500)
    private String fichierUrl;

    @Column(name = "type_piece", nullable = false, length = 30)
    private String typePiece;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_depose_par", nullable = false)
    private Utilisateur deposePar;

    @Column(name = "date_depot", nullable = false)
    private LocalDateTime dateDepot;
}