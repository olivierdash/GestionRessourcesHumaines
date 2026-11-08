package com.example.sirh.service;

import com.example.sirh.dto.PosteFilter;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Departement;
import com.example.sirh.model.Lieu;
import com.example.sirh.model.Poste;
import com.example.sirh.model.PosteMission;
import com.example.sirh.repository.DepartementRepository;
import com.example.sirh.repository.PosteMissionRepository;
import com.example.sirh.repository.PosteRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PosteReferentielService {

    private final PosteRepository posteRepository;
    private final PosteMissionRepository missionRepository;
    private final DepartementRepository departementRepository;
    private final LieuService lieuService;

    @Transactional(readOnly = true)
    public Page<Poste> listerPostes(PosteFilter filter, Pageable pageable) {
        return posteRepository.findWithFilters(filter, pageable);
    }

    @Transactional(readOnly = true)
    public List<Poste> getByDepartement(Integer idDepartement) {
        return posteRepository.findByDepartementIdDepartementOrderByIntituleAsc(idDepartement);
    }

    @Transactional(readOnly = true)
    public Poste trouverPoste(Integer id) {
        return posteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Poste introuvable"));
    }

    public Poste creerPoste(Poste poste) {
        Integer idDepartement = poste.getDepartement() != null
                ? poste.getDepartement().getIdDepartement()
                : null;
        Integer idLieu = poste.getLieu() != null
                ? poste.getLieu().getIdLieu()
                : null;

        Map<String, String> errors = new LinkedHashMap<>();
        validerChamps(poste.getIntitule(), idDepartement, poste.getDureeHebdo(), errors);

        if (errors.isEmpty()
                && posteRepository.existsByIntituleIgnoreCaseAndDepartement_IdDepartement(
                        poste.getIntitule().trim(), idDepartement)) {
            errors.put("intitule", "Ce poste existe déjà dans ce département.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        Departement departement = departementRepository.findById(idDepartement)
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable"));

        Lieu lieu = idLieu != null ? lieuService.getById(idLieu) : null;

        poste.setIntitule(poste.getIntitule().trim());
        poste.setDepartement(departement);
        poste.setLieu(lieu);

        return posteRepository.save(poste);
    }

    public Poste modifierPoste(Integer id, Poste poste) {
        Integer idDepartement = poste.getDepartement() != null
                ? poste.getDepartement().getIdDepartement()
                : null;
        Integer idLieu = poste.getLieu() != null
                ? poste.getLieu().getIdLieu()
                : null;

        Map<String, String> errors = new LinkedHashMap<>();
        validerChamps(poste.getIntitule(), idDepartement, poste.getDureeHebdo(), errors);

        if (errors.isEmpty()
                && posteRepository.existsByIntituleIgnoreCaseAndDepartement_IdDepartementAndIdPosteNot(
                        poste.getIntitule().trim(), idDepartement, id)) {
            errors.put("intitule", "Ce poste existe déjà dans ce département.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        Poste existing = trouverPoste(id);
        Departement departement = departementRepository.findById(idDepartement)
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable"));

        existing.setIntitule(poste.getIntitule().trim());
        existing.setDepartement(departement);
        existing.setDateEffet(poste.getDateEffet());
        existing.setLieu(idLieu != null ? lieuService.getById(idLieu) : null);
        existing.setDureeHebdo(poste.getDureeHebdo());

        return posteRepository.save(existing);
    }

    public PosteMission ajouterMission(Integer idPoste, String titre) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (titre == null || titre.isBlank()) {
            errors.put("titre", "Le titre de la mission est obligatoire.");
        } else if (missionRepository.existsByPosteIdPosteAndTitreIgnoreCase(idPoste, titre.trim())) {
            errors.put("titre", "Cette mission existe déjà pour ce poste.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        PosteMission mission = PosteMission.builder()
                .poste(trouverPoste(idPoste))
                .titre(titre.trim())
                .build();

        return missionRepository.save(mission);
    }

    public void supprimerMission(Integer idPoste, Integer idMission) {
        PosteMission mission = missionRepository.findById(idMission)
                .orElseThrow(() -> new IllegalArgumentException("Mission introuvable"));

        if (!mission.getPoste().getIdPoste().equals(idPoste)) {
            throw new IllegalArgumentException("Cette mission n'appartient pas à ce poste.");
        }

        missionRepository.delete(mission);
    }

    @Transactional(readOnly = true)
    public List<PosteMission> getMissionsByPosteId(Integer idPoste) {
        return missionRepository.findByPosteIdPoste(idPoste);
    }

    private void validerChamps(String intitule, Integer idDepartement, Integer dureeHebdo,
                               Map<String, String> errors) {
        if (intitule == null || intitule.isBlank()) {
            errors.put("intitule", "L'intitulé du poste est obligatoire.");
        }
        if (idDepartement == null) {
            errors.put("departement.idDepartement", "Le département est obligatoire.");
        }
        if (dureeHebdo != null && dureeHebdo <= 0) {
            errors.put("dureeHebdo", "La durée hebdomadaire doit être positive.");
        }
    }
}