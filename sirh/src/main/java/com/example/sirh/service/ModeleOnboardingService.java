package com.example.sirh.service;

import com.example.sirh.model.ModeleOnboarding;
import com.example.sirh.model.ModeleTacheOnboarding;
import com.example.sirh.model.Role;
import com.example.sirh.model.TypeDocument;
import com.example.sirh.repository.ModeleOnboardingRepository;
import com.example.sirh.repository.ModeleTacheOnboardingRepository;
import com.example.sirh.repository.RoleRepository;
import com.example.sirh.repository.TypeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModeleOnboardingService {

    private final ModeleOnboardingRepository modeleRepository;
    private final ModeleTacheOnboardingRepository tacheRepository;
    private final RoleRepository roleRepository;
    private final TypeDocumentRepository typeDocumentRepository;

    @Transactional(readOnly = true)
    public List<ModeleOnboarding> lister() {
        return modeleRepository.findAllByOrderByNomAsc();
    }

    @Transactional(readOnly = true)
    public ModeleOnboarding trouver(Integer id) {
        return modeleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Modèle d'onboarding introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<ModeleTacheOnboarding> listerTaches(Integer idModele) {
        trouver(idModele);
        return tacheRepository.findByModele_IdOrderByOrdreAsc(idModele);
    }

    @Transactional
    public ModeleOnboarding creer(String nom) {
        String nomValide = validerNom(nom);
        if (modeleRepository.findByNomIgnoreCase(nomValide).isPresent()) {
            throw new IllegalArgumentException("Un modèle porte déjà ce nom.");
        }
        return modeleRepository.save(ModeleOnboarding.builder()
                .nom(nomValide)
                .actif(false)
                .build());
    }

    @Transactional
    public ModeleOnboarding renommer(Integer id, String nom) {
        ModeleOnboarding modele = trouver(id);
        String nomValide = validerNom(nom);
        modeleRepository.findByNomIgnoreCase(nomValide)
                .filter(existant -> !existant.getId().equals(id))
                .ifPresent(existant -> {
                    throw new IllegalArgumentException("Un modèle porte déjà ce nom.");
                });
        modele.setNom(nomValide);
        return modeleRepository.save(modele);
    }

    @Transactional
    public void activer(Integer id) {
        ModeleOnboarding cible = trouver(id);
        if (tacheRepository.findByModele_IdOrderByOrdreAsc(id).isEmpty()) {
            throw new IllegalStateException("Ajoutez au moins une tâche avant d'activer ce modèle.");
        }

        List<ModeleOnboarding> actifs = modeleRepository.findAllByActifTrue();
        actifs.stream()
                .filter(modele -> !modele.getId().equals(id))
                .forEach(modele -> modele.setActif(false));
        modeleRepository.saveAll(actifs);
        modeleRepository.flush();

        cible.setActif(true);
        modeleRepository.save(cible);
    }

    @Transactional
    public ModeleTacheOnboarding ajouterTache(Integer idModele, String titre, Integer idRoleResponsable,
            Integer delaiJours, Boolean obligatoire, Integer idTypeDocumentRequis, Integer ordre) {
        ModeleOnboarding modele = trouver(idModele);
        validerTache(titre, delaiJours, obligatoire, ordre);
        return tacheRepository.save(creerDefinition(modele, null, titre, idRoleResponsable,
                delaiJours, obligatoire, idTypeDocumentRequis, ordre));
    }

    @Transactional
    public ModeleTacheOnboarding modifierTache(Integer idModele, Integer idTache, String titre,
            Integer idRoleResponsable, Integer delaiJours, Boolean obligatoire,
            Integer idTypeDocumentRequis, Integer ordre) {
        validerTache(titre, delaiJours, obligatoire, ordre);
        ModeleTacheOnboarding definition = tacheRepository.findByIdAndModele_Id(idTache, idModele)
                .orElseThrow(() -> new IllegalArgumentException("Tâche du modèle introuvable : " + idTache));
        ModeleOnboarding modele = trouver(idModele);
        copierDefinition(definition, modele, titre, idRoleResponsable, delaiJours,
                obligatoire, idTypeDocumentRequis, ordre);
        return tacheRepository.save(definition);
    }

    @Transactional
    public void supprimerTache(Integer idModele, Integer idTache) {
        ModeleOnboarding modele = trouver(idModele);
        ModeleTacheOnboarding definition = tacheRepository.findByIdAndModele_Id(idTache, idModele)
                .orElseThrow(() -> new IllegalArgumentException("Tâche du modèle introuvable : " + idTache));
        if (modele.isActif() && tacheRepository.countByModele_Id(idModele) <= 1) {
            throw new IllegalStateException("Un modèle actif doit conserver au moins une tâche.");
        }
        tacheRepository.delete(definition);
    }

    private ModeleTacheOnboarding creerDefinition(ModeleOnboarding modele, Integer id,
            String titre, Integer idRoleResponsable, Integer delaiJours, Boolean obligatoire,
            Integer idTypeDocumentRequis, Integer ordre) {
        ModeleTacheOnboarding definition = ModeleTacheOnboarding.builder().id(id).build();
        copierDefinition(definition, modele, titre, idRoleResponsable, delaiJours,
                obligatoire, idTypeDocumentRequis, ordre);
        return definition;
    }

    private void copierDefinition(ModeleTacheOnboarding definition, ModeleOnboarding modele,
            String titre, Integer idRoleResponsable, Integer delaiJours, Boolean obligatoire,
            Integer idTypeDocumentRequis, Integer ordre) {
        Role role = roleRepository.findById(idRoleResponsable)
                .orElseThrow(() -> new IllegalArgumentException("Rôle responsable introuvable."));
        TypeDocument document = idTypeDocumentRequis == null ? null
                : typeDocumentRepository.findById(idTypeDocumentRequis)
                        .orElseThrow(() -> new IllegalArgumentException("Type de document introuvable."));
        definition.setModele(modele);
        definition.setTitre(titre.trim());
        definition.setRoleResponsable(role);
        definition.setDelaiJours(delaiJours);
        definition.setObligatoire(obligatoire);
        definition.setTypeDocumentRequis(document);
        definition.setOrdre(ordre);
    }

    private void validerTache(String titre, Integer delaiJours, Boolean obligatoire, Integer ordre) {
        if (titre == null || titre.isBlank() || titre.trim().length() > 255) {
            throw new IllegalArgumentException("Le titre de la tâche est obligatoire (255 caractères maximum).");
        }
        if (delaiJours == null || delaiJours < 0) {
            throw new IllegalArgumentException("Le délai doit être supérieur ou égal à zéro.");
        }
        if (obligatoire == null) {
            throw new IllegalArgumentException("Le caractère obligatoire de la tâche est requis.");
        }
        if (ordre == null || ordre < 1) {
            throw new IllegalArgumentException("L'ordre doit être supérieur ou égal à 1.");
        }
    }

    private String validerNom(String nom) {
        if (nom == null || nom.isBlank() || nom.trim().length() > 100) {
            throw new IllegalArgumentException("Le nom du modèle est obligatoire (100 caractères maximum).");
        }
        return nom.trim();
    }
}
