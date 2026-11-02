package com.example.sirh.service;

import com.example.sirh.model.Departement;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DepartementRepository;
import com.example.sirh.repository.EmployeRepository;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class DepartementService {

    private final DepartementRepository departementRepository;
    private final EmployeRepository employeRepository;
    private final UtilisateurService utilisateurService;

    public List<Departement> getAllDepartements() {
        return departementRepository.findAll();
    }

    public Departement getById(Integer id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Département introuvable"));
    }

    @Transactional
    public Departement createDepartement(String titre, Integer idManager) {
        String titreNettoye = titre.trim();

        // Vérification de l'unicité
        if (departementRepository.existsByTitreIgnoreCase(titreNettoye)) {
            throw new IllegalArgumentException("Un département avec ce titre existe déjà.");
        }

        Departement departement = new Departement();
        departement.setTitre(titreNettoye);

        if (idManager != null) {
            Utilisateur manager = utilisateurService.getById(idManager);
            departement.setManager(manager);
        }

        return departementRepository.save(departement);
    }

    @Transactional
    public Departement updateDepartement(Integer id, String titre, Integer idManager) {
        String titreNettoye = titre.trim();

        // Vérification de l'unicité en excluant l'ID actuel
        if (departementRepository.existsByTitreIgnoreCaseAndIdDepartementNot(titreNettoye, id)) {
            throw new IllegalArgumentException("Un autre département porte déjà ce titre.");
        }

        Departement departement = getById(id);
        departement.setTitre(titreNettoye);

        if (idManager != null) {
            Utilisateur manager = utilisateurService.getById(idManager);
            departement.setManager(manager);
        } else {
            departement.setManager(null);
        }

        return departementRepository.save(departement);
    }

    public long countEmployesByDepartement(Integer idDepartement) {
        return employeRepository.countByCandidature_DemandeRecrutement_Poste_Departement_IdDepartement(idDepartement);
    }

    public boolean isTitreDisponible(String titre, Integer idDepartement) {
        if (idDepartement == null) {
            return !departementRepository.existsByTitreIgnoreCase(titre.trim());
        } else {
            return !departementRepository.existsByTitreIgnoreCaseAndIdDepartementNot(titre.trim(), idDepartement);
        }
    }
}