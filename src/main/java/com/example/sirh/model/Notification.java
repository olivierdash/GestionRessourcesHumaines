package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "notification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Integer idNotification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_destinataire_candidat")
    private Candidat destinataireCandidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_destinataire_utilisateur")
    private Utilisateur destinataireUtilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_notification", nullable = false)
    private TypeNotification typeNotification;

    @Column(nullable = false)
    private String message;
    @Column(name = "lien_action")
    private String lienAction;
    @Builder.Default
    @Column(name = "est_lu")
    private Boolean estLu = false;
    @Column(name = "date_creation", insertable = false, updatable = false)
    private LocalDateTime dateCreation;
}