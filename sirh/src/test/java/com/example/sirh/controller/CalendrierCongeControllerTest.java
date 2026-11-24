package com.example.sirh.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.example.sirh.model.Employe;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.service.AbsenceService;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.PlanningCongeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CalendrierCongeControllerTest {

    @Mock
    private CongeAccessService accessService;
    @Mock
    private EmployeService employeService;
    @Mock
    private AbsenceService absenceService;
    @Mock
    private PlanningCongeService planningCongeService;

    @InjectMocks
    private CalendrierCongeController controller;

    @Test
    void filtreLesAbsencesEtDemandesSurLeSalarieChoisi() throws Exception {
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(1).build();
        List<Employe> employes = List.of(
                Employe.builder().idEmployer(11).build(),
                Employe.builder().idEmployer(22).build());
        LocalDate debut = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(accessService.utilisateurCourant()).thenReturn(utilisateur);
        when(accessService.estRhOuDg(utilisateur)).thenReturn(true);
        when(employeService.getAllEmployesList()).thenReturn(employes);
        when(absenceService.absencesPeriode(List.of(22), debut, fin)).thenReturn(List.of());
        when(planningCongeService.demandesEnAttentePeriode(debut, fin)).thenReturn(List.of());

        var result = mockMvc.perform(get("/conges/calendrier")
                        .param("periode", "2026-11")
                        .param("employeId", "22"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/calendrier"))
                .andExpect(model().attribute("employes", employes))
                .andExpect(model().attribute("filtreEmployeId", 22))
                .andExpect(model().attribute("semaines", 6))
                .andReturn();
        Map<?, ?> absencesParJour = (Map<?, ?>) result.getModelAndView().getModel()
                .get("absencesParJour");
        assertEquals(30, absencesParJour.size());
        assertTrue(absencesParJour.containsKey(1));
        assertTrue(absencesParJour.containsKey(30));
        List<?> jours = (List<?>) result.getModelAndView().getModel().get("jours");
        assertEquals(30, jours.size());
        assertEquals(debut, jours.get(0));
        assertEquals(fin, jours.get(29));
        verify(absenceService).absencesPeriode(List.of(22), debut, fin);
    }

    @Test
    void filtreVideAfficheTousLesSalariesDuPerimetre() throws Exception {
        Utilisateur utilisateur = Utilisateur.builder().idUtilisateur(1).build();
        List<Employe> employes = List.of(
                Employe.builder().idEmployer(11).build(),
                Employe.builder().idEmployer(22).build());
        LocalDate debut = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 30);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(accessService.utilisateurCourant()).thenReturn(utilisateur);
        when(accessService.estRhOuDg(utilisateur)).thenReturn(true);
        when(employeService.getAllEmployesList()).thenReturn(employes);
        when(absenceService.absencesPeriode(List.of(11, 22), debut, fin)).thenReturn(List.of());
        when(planningCongeService.demandesEnAttentePeriode(debut, fin)).thenReturn(List.of());

        mockMvc.perform(get("/conges/calendrier")
                        .param("periode", "2026-11")
                        .param("employeId", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("filtreEmployeId", (Object) null));
        verify(absenceService).absencesPeriode(List.of(11, 22), debut, fin);
    }
}
