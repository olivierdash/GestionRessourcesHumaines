package com.example.sirh.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "type_notification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TypeNotification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_notification")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String libelle;
}