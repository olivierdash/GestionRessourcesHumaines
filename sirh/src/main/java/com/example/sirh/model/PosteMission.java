package com.example.sirh.model;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "poste_mission")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PosteMission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mission")
    private Integer idMission;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;
    @Column(nullable = false)
    private String titre;
}
