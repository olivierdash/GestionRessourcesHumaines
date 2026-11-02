package com.example.sirh.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.sirh.model.HistoriqueDemandeRecrutement;
import com.example.sirh.repository.HistoriqueDemandeRecrutementRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class HistoriqueDemandeRecrutementService {
    private HistoriqueDemandeRecrutementRepository historiqueRepository;

    public List<HistoriqueDemandeRecrutement> getByDemandeId(Integer idDemande) {
        return historiqueRepository.getByIdDemandeRecrutement(idDemande);
    }
}