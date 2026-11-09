package com.example.sirh.service;

import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.HistoriqueTypeContrat;
import com.example.sirh.model.TypeContrat;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.HistoriqueTypeContratRepository;
import com.example.sirh.repository.TypeContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TypeContratService {

    private final TypeContractRepository typeContratRepository;
    private final HistoriqueTypeContratRepository historiqueRepository;
    private final UtilisateurService utilisateurService;

    @Transactional(readOnly = true)
    public List<TypeContrat> getAllTypes() {
        return typeContratRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TypeContrat getById(Integer id) {
        return typeContratRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type de contrat introuvable : " + id));
    }

    @Transactional
    public TypeContrat modifierDureeMax(Integer id, Integer dureeMaxMois, Integer idUtilisateur) {
        TypeContrat typeContrat = getById(id);

        if (dureeMaxMois != null && dureeMaxMois <= 0) {
            Map<String, String> errors = new LinkedHashMap<>();
            errors.put("dureeMaxMois", "La durée maximale doit être positive.");
            throw new ValidationMetierException(errors);
        }

        Integer ancienneDuree = typeContrat.getDureeMaxMois();

        if (Objects.equals(ancienneDuree, dureeMaxMois)) {
            return typeContrat;
        }

        typeContrat.setDureeMaxMois(dureeMaxMois);
        TypeContrat saved = typeContratRepository.save(typeContrat);

        Utilisateur utilisateur = utilisateurService.getById(idUtilisateur);
        historiqueRepository.save(HistoriqueTypeContrat.builder()
                .typeContrat(saved)
                .utilisateur(utilisateur)
                .ancienneDuree(ancienneDuree)
                .nouvelleDuree(dureeMaxMois)
                .build());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<HistoriqueTypeContrat> getHistorique(Integer idTypeContrat) {
        return historiqueRepository.findByTypeContratIdOrderByDateModificationDesc(idTypeContrat);
    }
}