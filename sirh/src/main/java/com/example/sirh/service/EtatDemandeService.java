package com.example.sirh.service;

import com.example.sirh.model.EtatDemande;
import com.example.sirh.repository.EtatDemandeRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor 

public class EtatDemandeService {

    private EtatDemandeRepository etatDemandeRepository;

    public EtatDemande getByIntitule(String s) {
        return etatDemandeRepository.findByIntituleIgnoreCase(s)
                .orElseThrow(() -> new NoSuchElementException("L'état " + s + " n\'existe pas en base de données."));
    }

    public List<EtatDemande> getByIntitules(String[] intitules) {
        return etatDemandeRepository.findByIntituleInIgnoreCase(intitules);
    }

    public EtatDemande getById(Integer id){
        return etatDemandeRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Etat avec id " + id + " introuvable "));
    }
}