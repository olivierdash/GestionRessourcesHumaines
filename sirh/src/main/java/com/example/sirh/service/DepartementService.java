package com.example.sirh.service;

import com.example.sirh.model.Departement;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DepartementRepository;

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
    private final UtilisateurService utilisateurService;

    @Transactional(readOnly = true)
    public List<Departement> getAllDepartements() {
        return departementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Departement getById(Integer id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Département introuvable"));
    }

    @Transactional
    public Departement createDepartement(String titre, Integer idManager) {
        String titreNettoye = titre.trim();

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

    @Transactional(readOnly = true)
    public boolean isTitreDisponible(String titre, Integer idDepartement) {
        if (idDepartement == null) {
            return !departementRepository.existsByTitreIgnoreCase(titre.trim());
        }
        return !departementRepository.existsByTitreIgnoreCaseAndIdDepartementNot(
                titre.trim(), idDepartement);
    }
}