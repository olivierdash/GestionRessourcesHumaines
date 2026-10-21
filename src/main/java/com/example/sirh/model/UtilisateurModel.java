package com.example.sirh.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Data 
@Table (name = "utilisateur")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class UtilisateurModel {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id_utilisateur;
    private String nom;
    private String email;
    private String mot_de_passe;
    private String role;

    // RELATION : 1 Utilisateur -> PLUSIEURS Demandes
    // "mappedBy" indique que c'est la classe DemandeRecrutement qui possède la clé étrangère.
    //@OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    //private List<DemandeRecrutement> demandes;
}
