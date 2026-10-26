# voici la configuration de spring boot :
- Spring web
- Spring Session JDBC 
- thymeleaf
- Spring Data jpa
**vu qu'on ne sait pas je suppose quel base utilisé j'ai mis deux méthodes**  
- sqlite driver 
- postgres driver (car on a beaucoup de transaction)

**pour la session**
- Spring Security (pour pouvoir utilisé la session des utilisateurs : super user , candidat , manager , etc ...)
- lombok (pour ne pas s'embêter avec les getter et les setters à écrire car il s'occupe de cela et crée les tables directement via le code java )  
ex :  
```java
@Data
public class Candidat {
    private Long id_candidat;
    private String nom;
    private String prenom;
}

// @Data gère directement les setters , getters et les méthodes tostring, equals et hashcode un gain de temps énorme 
```

- validation  
vérification des donnés avant insertion mail valide ,salaire positif ,un champs vide alors qu'il est obligatoire  
ex :  
```java 
public class Candidat {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @Email(message = "Format d'email invalide")
    private String email;

    @Min(value = 0, message = "Le salaire doit être positif")
    private Double salairePropose; // exemple
}
```

exemple concret dans l'utilisation de Lombok et validation en springBoot  
```java
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data // <-- Lombok génère les getters/setters automatiquement
public class CandidatDTO { // J'utilise DTO ici, je t'explique pourquoi plus bas
    
    private Long id_candidat;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit faire entre 2 et 50 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    private String email;

    @Size(min = 10, max = 20, message = "Le numéro de téléphone doit être valide")
    private String telephone;
}
```

pour consulter le truc c'est localhost:8080//offres 