package com.example.sirh.dto;

import java.util.List;
import java.util.Map;

import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.model.GrilleSalarial;

public class ValidationPageData {
    private List<DemandeRecrutement> demandes;
    private Map<Integer, GrilleSalarial> grilles;
    public ValidationPageData(List<DemandeRecrutement> demandes, Map<Integer, GrilleSalarial> grilles) {
        this.demandes = demandes;
        this.grilles = grilles;
    }
    public List<DemandeRecrutement> getDemandes() {
        return demandes;
    }
    public void setDemandes(List<DemandeRecrutement> demandes) {
        this.demandes = demandes;
    }
    public Map<Integer, GrilleSalarial> getGrilles() {
        return grilles;
    }
    public void setGrilles(Map<Integer, GrilleSalarial> grilles) {
        this.grilles = grilles;
    }
}