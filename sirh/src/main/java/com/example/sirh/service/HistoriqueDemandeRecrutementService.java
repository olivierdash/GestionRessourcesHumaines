package com.example.sirh.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.model.HistoriqueDemandeRecrutement;
import com.example.sirh.repository.HistoriqueDemandeRecrutementRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class HistoriqueDemandeRecrutementService {

    private final HistoriqueDemandeRecrutementRepository historiqueRepository;

    @Transactional(readOnly = true)
    public List<HistoriqueDemandeRecrutement> getByDemandeId(Integer idDemande) {
        return historiqueRepository.getByIdDemandeRecrutement(idDemande);
    }

    @Transactional
    public HistoriqueDemandeRecrutement save(HistoriqueDemandeRecrutement historique) {
        return historiqueRepository.save(historique);
    }
}