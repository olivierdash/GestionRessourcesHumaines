package com.example.sirh.service;

import com.example.sirh.dto.ContextePaie;
import com.example.sirh.dto.PointageCalculDto;
import com.example.sirh.model.*;
import com.example.sirh.repository.ContratRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextePaieServiceTest {

    @Mock
    private ContratRepository contratRepository;
    @Mock
    private EmployeService employeService;
    @Mock
    private PointageService pointageService;

    @InjectMocks
    private ContextePaieService service;

    private Employe employe;
    private Contrat contrat;
    private Poste poste;
    private TypeContrat typeContrat;
    private StatutContrat statutContrat;

    @BeforeEach
    void setUp() {
        employe = new Employe();
        employe.setIdEmployer(1);
        employe.setMatricule("EMP-2025-0001");

        typeContrat = new TypeContrat();
        typeContrat.setId(1);
        typeContrat.setLibelle("CDI");

        statutContrat = new StatutContrat();
        statutContrat.setId(1);
        statutContrat.setLibelle("actif");

        poste = new Poste();
        poste.setIdPoste(1);

        contrat = new Contrat();
        contrat.setIdContract(1);
        contrat.setEmploye(employe);
        contrat.setPoste(poste);
        contrat.setTypeContrat(typeContrat);
        contrat.setStatut(statutContrat);
        contrat.setDateDebut(LocalDate.of(2024, 1, 1));
        contrat.setDateFin(null);
        contrat.setSalaireMensuel(new BigDecimal("1000000"));
    }

    @Test
    void charger_calculeTauxHoraireEtJournalier() {
        LocalDate debut = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 1, 31);

        when(employeService.getById(1)).thenReturn(employe);
        when(contratRepository.findContratsCouvrantPeriode(1, debut, fin))
                .thenReturn(List.of(contrat));
        when(pointageService.listerEntre(1, debut, fin)).thenReturn(List.of());

        ContextePaie ctx = service.charger(1, debut, fin);

        // Salaire de base
        assertEquals(0, new BigDecimal("1000000").compareTo(ctx.salaireBaseMensuel()));

        // Taux horaire = 1 000 000 / 173,33 = 5 769,05 (arrondi 2 décimales)
        assertEquals(0, new BigDecimal("5769.34").compareTo(ctx.tauxHoraire()));
        // Taux journalier = 1 000 000 / 30 = 33 333,33
        assertEquals(0, new BigDecimal("33333.33").compareTo(ctx.tauxJournalier()));

        // Métadonnées
        assertEquals(1, ctx.idEmployer());
        assertEquals("EMP-2025-0001", ctx.matricule());
        assertEquals(1, ctx.contrat().idContract());
    }

    @Test
    void charger_echoueSiAucunContrat() {
        when(employeService.getById(1)).thenReturn(employe);
        when(contratRepository.findContratsCouvrantPeriode(any(), any(), any()))
                .thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.charger(1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)));

        assertTrue(ex.getMessage().contains("Aucun contrat"));
    }

    @Test
    void charger_echoueSiDateDebutApresDateFin() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.charger(1, LocalDate.of(2025, 1, 31), LocalDate.of(2025, 1, 1)));

        assertTrue(ex.getMessage().contains("Date de début"));
    }

    @Test
    void charger_mappeLesPointagesEtHeuresSup() {
        LocalDate debut = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 1, 31);

        Pointage p = new Pointage();
        p.setDatePointage(LocalDate.of(2025, 1, 15));
        p.setHeureEntree(LocalTime.of(8, 0));
        p.setHeureSortie(LocalTime.of(17, 0));

        PointageCalculDto calc = new PointageCalculDto();
        calc.setMinutesTravaillees(480);
        calc.setMinutesRetard(10);
        calc.setMinutesSupplementaires(60);

        when(employeService.getById(1)).thenReturn(employe);
        when(contratRepository.findContratsCouvrantPeriode(1, debut, fin))
                .thenReturn(List.of(contrat));
        when(pointageService.listerEntre(1, debut, fin)).thenReturn(List.of(p));
        when(pointageService.calculer(p)).thenReturn(calc);

        ContextePaie ctx = service.charger(1, debut, fin);

        assertEquals(1, ctx.pointages().size());
        ContextePaie.PointageInfo pi = ctx.pointages().get(0);
        assertEquals(480, pi.minutesTravaillees());
        assertEquals(10, pi.minutesRetard());
        assertEquals(60, pi.minutesSup());

        // 60 min sup → 1 ligne HeureSupInfo
        assertEquals(1, ctx.heuresSupplementaires().size());
        assertEquals(60, ctx.heuresSupplementaires().get(0).minutes());
    }
}