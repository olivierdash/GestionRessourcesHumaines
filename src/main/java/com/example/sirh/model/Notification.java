package com.example.sirh.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Integer idNotification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_destinataire_candidat")
    private Candidat destinataireCandidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_destinataire_utilisateur")
    private Utilisateur destinataireUtilisateur;

    @Column(name = "type_notification", nullable = false, length = 50)
    private String typeNotification;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "lien_action")
    private String lienAction;

    @Column(name = "est_lu")
    @Builder.Default
    private Boolean estLu = false;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;
}