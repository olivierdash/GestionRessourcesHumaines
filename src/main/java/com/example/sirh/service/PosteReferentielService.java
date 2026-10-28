package com.example.sirh.service;

import com.example.sirh.model.Departement;
import com.example.sirh.model.Poste;
import com.example.sirh.model.PosteMission;
import com.example.sirh.repository.DepartementRepository;
import com.example.sirh.repository.PosteMissionRepository;
import com.example.sirh.repository.PosteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PosteReferentielService {

    private final PosteRepository posteRepository;
    private final PosteMissionRepository missionRepository;
    private final DepartementRepository departementRepository;

    @Transactional(readOnly = true)
    public List<Poste> listerPostes() {
        return posteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Poste trouverPoste(Integer id) {
        return posteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Poste introuvable"));
    }

    public Poste creerPoste(
            String intitule,
            Integer idDepartement,
            LocalDate dateEffet,
            String lieu,
            Integer dureeHebdo) {

        verifierChamps(intitule, idDepartement, dureeHebdo);

        if (posteRepository
                .existsByIntituleIgnoreCaseAndDepartement_IdDepartement(
                        intitule.trim(), idDepartement)) {
            throw new IllegalArgumentException(
                    "Ce poste existe déjà dans ce département.");
        }

        Departement departement = departementRepository.findById(idDepartement)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Département introuvable"));

        Poste poste = Poste.builder()
                .intitule(intitule.trim())
                .departement(departement)
                .dateEffet(dateEffet)
                .lieu(lieu)
                .dureeHebdo(dureeHebdo)
                .build();

        return posteRepository.save(poste);
    }

    public Poste modifierPoste(
            Integer id,
            String intitule,
            Integer idDepartement,
            LocalDate dateEffet,
            String lieu,
            Integer dureeHebdo) {

        verifierChamps(intitule, idDepartement, dureeHebdo);

        if (posteRepository
                .existsByIntituleIgnoreCaseAndDepartement_IdDepartementAndIdPosteNot(
                        intitule.trim(), idDepartement, id)) {
            throw new IllegalArgumentException(
                    "Ce poste existe déjà dans ce département.");
        }

        Poste poste = trouverPoste(id);
        Departement departement = departementRepository.findById(idDepartement)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Département introuvable"));

        poste.setIntitule(intitule.trim());
        poste.setDepartement(departement);
        poste.setDateEffet(dateEffet);
        poste.setLieu(lieu);
        poste.setDureeHebdo(dureeHebdo);

        return posteRepository.save(poste);
    }

    public PosteMission ajouterMission(Integer idPoste, String titre) {
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException(
                    "Le titre de la mission est obligatoire.");
        }

        if (missionRepository.existsByPosteIdPosteAndTitreIgnoreCase(
                idPoste, titre.trim())) {
            throw new IllegalArgumentException(
                    "Cette mission existe déjà pour ce poste.");
        }

        PosteMission mission = PosteMission.builder()
                .poste(trouverPoste(idPoste))
                .titre(titre.trim())
                .build();

        return missionRepository.save(mission);
    }

    public void supprimerMission(Integer idPoste, Integer idMission) {
        PosteMission mission = missionRepository.findById(idMission)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mission introuvable"));

        if (!mission.getPoste().getIdPoste().equals(idPoste)) {
            throw new IllegalArgumentException(
                    "Cette mission n'appartient pas à ce poste.");
        }

        missionRepository.delete(mission);
    }

    private void verifierChamps(
            String intitule,
            Integer idDepartement,
            Integer dureeHebdo) {

        if (intitule == null || intitule.isBlank()) {
            throw new IllegalArgumentException(
                    "L'intitulé du poste est obligatoire.");
        }

        if (idDepartement == null) {
            throw new IllegalArgumentException(
                    "Le département est obligatoire.");
        }

        if (dureeHebdo != null && dureeHebdo <= 0) {
            throw new IllegalArgumentException(
                    "La durée hebdomadaire doit être positive.");
        }
    }
}