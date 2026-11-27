package com.example.sirh.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.dto.CongeDashboardDto;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.Absence;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.enums.StatutAbsence;
import com.example.sirh.model.enums.StatutDemandeConge;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CongeDashboardService {

    private final SoldeCongeService soldeCongeService;
    private final DemandeCongeService demandeCongeService;
    private final AbsenceService absenceService;

    @Transactional
    public CongeDashboardDto tableauDeBord(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getEmploye() == null) {
            throw new ValidationMetierException(java.util.Map.of(
                    "employe", "Votre compte n'est lié à aucun salarié. Contactez les RH."));
        }
        Integer employeId = utilisateur.getEmploye().getIdEmployer();
        int annee = LocalDate.now().getYear();

        CongeDashboardDto dto = new CongeDashboardDto();
        dto.setSoldes(soldeCongeService.soldesEmploye(employeId, annee));

        List<DemandeConge> demandes = demandeCongeService.listerMesDemandes(employeId);
        dto.setDemandes(demandes.stream().limit(10).toList());
        dto.setDemandesEnAttente(demandes.stream()
                .filter(d -> d.getStatut() == StatutDemandeConge.SOUMISE
                        || d.getStatut() == StatutDemandeConge.VALIDEE_MANAGER)
                .count());

        LocalDate aujourdHui = LocalDate.now();
        dto.setAbsencesEnCours(absenceService.absencesDe(employeId).stream()
                .filter(a -> a.getStatut() != StatutAbsence.ANNULEE)
                .filter(a -> !a.getDateDebut().isAfter(aujourdHui)
                        && !a.getDateFin().isBefore(aujourdHui))
                .count());
        return dto;
    }
}
