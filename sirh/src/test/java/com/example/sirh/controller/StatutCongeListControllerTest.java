package com.example.sirh.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Map;

import com.example.sirh.dto.AbsenceFilter;
import com.example.sirh.dto.DemandeCongeFilter;
import com.example.sirh.model.Absence;
import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.Employe;
import com.example.sirh.model.TypeConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.enums.StatutAbsence;
import com.example.sirh.model.enums.StatutDemandeConge;
import com.example.sirh.service.AbsenceService;
import com.example.sirh.service.CalculDureeCongeService;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.CongeDashboardService;
import com.example.sirh.service.DemandeCongeService;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.JustificatifCongeService;
import com.example.sirh.service.PlanningCongeService;
import com.example.sirh.service.SoldeCongeService;
import com.example.sirh.service.TypeCongeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class StatutCongeListControllerTest {

    @Mock
    private CongeAccessService accessService;
    @Mock
    private DemandeCongeService demandeCongeService;
    @Mock
    private JustificatifCongeService justificatifCongeService;
    @Mock
    private SoldeCongeService soldeCongeService;
    @Mock
    private PlanningCongeService planningCongeService;
    @Mock
    private CongeDashboardService dashboardService;
    @Mock
    private AbsenceService absenceService;
    @Mock
    private TypeCongeService typeCongeService;
    @Mock
    private EmployeService employeService;
    @Mock
    private CalculDureeCongeService calculDureeCongeService;

    @InjectMocks
    private CongeValidationController congeValidationController;
    @InjectMocks
    private MesCongeController mesCongeController;
    @InjectMocks
    private CongeDemandeController congeDemandeController;
    @InjectMocks
    private AbsenceController absenceController;

    @Test
    void filtreLaFileDeValidationSelonLeStatutSansElargirSonPerimetre() throws Exception {
        Utilisateur utilisateur = mock(Utilisateur.class);
        DemandeConge valideeManager = mock(DemandeConge.class);
        when(accessService.utilisateurCourant()).thenReturn(utilisateur);
        when(demandeCongeService.fileValidation(eq(utilisateur),
                eq(StatutDemandeConge.VALIDEE_MANAGER), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(valideeManager), PageRequest.of(0, 10), 1));
        when(demandeCongeService.compterEnAttente(utilisateur)).thenReturn(2L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(congeValidationController).build();

        var result = mockMvc.perform(get("/conges/validations").param("statut", "VALIDEE_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/validations/index"))
                .andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();
        Page<?> demandes = assertInstanceOf(Page.class, model.get("demandes"));
        assertEquals(List.of(valideeManager), demandes.getContent());
        assertEquals(1, demandes.getTotalElements());
        assertEquals(StatutDemandeConge.VALIDEE_MANAGER, model.get("filtreStatut"));
    }

    @Test
    void pagineLaFileApresApplicationDuFiltreDeStatut() throws Exception {
        Utilisateur utilisateur = mock(Utilisateur.class);
        List<DemandeConge> contenuPage = List.of(
                mock(DemandeConge.class),
                mock(DemandeConge.class),
                mock(DemandeConge.class));
        PageRequest pageable = PageRequest.of(1, 3);
        when(accessService.utilisateurCourant()).thenReturn(utilisateur);
        when(demandeCongeService.fileValidation(utilisateur,
                StatutDemandeConge.SOUMISE, pageable))
                .thenReturn(new PageImpl<>(contenuPage, pageable, 8));
        when(demandeCongeService.compterEnAttente(utilisateur)).thenReturn(12L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(congeValidationController).build();

        var result = mockMvc.perform(get("/conges/validations")
                .param("statut", "SOUMISE")
                .param("page", "1")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/validations/index"))
                .andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();
        Page<?> demandes = assertInstanceOf(Page.class, model.get("demandes"));
        assertEquals(contenuPage, demandes.getContent());
        assertEquals(8, demandes.getTotalElements());
        assertEquals(1, demandes.getNumber());
        assertEquals(3, demandes.getSize());
        assertEquals(StatutDemandeConge.SOUMISE, model.get("filtreStatut"));
        verify(demandeCongeService).fileValidation(utilisateur,
                StatutDemandeConge.SOUMISE, pageable);
    }

    @Test
    void laRouteDemandeUnePageAuServiceAuLieuDeChargerTouteLaFile() throws Exception {
        Utilisateur utilisateur = mock(Utilisateur.class);
        PageRequest pageable = PageRequest.of(2, 5);
        Page<DemandeConge> file = new PageImpl<>(List.of(), pageable, 12);
        when(accessService.utilisateurCourant()).thenReturn(utilisateur);
        when(demandeCongeService.fileValidation(utilisateur, null, pageable)).thenReturn(file);
        when(demandeCongeService.compterEnAttente(utilisateur)).thenReturn(12L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(congeValidationController).build();

        mockMvc.perform(get("/conges/validations")
                .param("page", "2")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/validations/index"));

        verify(demandeCongeService).fileValidation(utilisateur, null, pageable);
    }

    @Test
    void filtreMesDemandesSurLeStatutApresLaLimitationAuSalarieCourant() throws Exception {
        Employe employe = mock(Employe.class);
        Utilisateur utilisateur = mock(Utilisateur.class);
        DemandeConge approuvee = demande(StatutDemandeConge.APPROUVEE);
        DemandeConge refusee = demande(StatutDemandeConge.REFUSEE);
        when(utilisateur.getEmploye()).thenReturn(employe);
        when(employe.getIdEmployer()).thenReturn(42);
        when(accessService.utilisateurCourant()).thenReturn(utilisateur);
        when(demandeCongeService.listerMesDemandes(42)).thenReturn(List.of(approuvee, refusee));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(mesCongeController).build();

        var result = mockMvc.perform(get("/mes-conges/demandes").param("statut", "APPROUVEE"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/mes/demandes"))
                .andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();
        assertEquals(List.of(approuvee), model.get("demandes"));
        assertEquals(StatutDemandeConge.APPROUVEE, model.get("filtreStatut"));
    }

    @Test
    void filtreMesAbsencesSurLeStatutApresLaLimitationAuSalarieCourant() throws Exception {
        Employe employe = mock(Employe.class);
        Utilisateur utilisateur = mock(Utilisateur.class);
        Absence annulee = mock(Absence.class);
        Absence declaree = mock(Absence.class);
        when(utilisateur.getEmploye()).thenReturn(employe);
        when(employe.getIdEmployer()).thenReturn(42);
        when(accessService.utilisateurCourant()).thenReturn(utilisateur);
        lenient().when(annulee.getStatut()).thenReturn(StatutAbsence.ANNULEE);
        lenient().when(declaree.getStatut()).thenReturn(StatutAbsence.DECLAREE);
        when(absenceService.absencesDe(42)).thenReturn(List.of(annulee, declaree));
        when(annulee.getId()).thenReturn(81);
        when(typeCongeService.listerAbsencesActives()).thenReturn(List.of());
        when(justificatifCongeService.listerPourAbsence(81)).thenReturn(List.of());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(mesCongeController).build();

        var result = mockMvc.perform(get("/mes-conges/absences").param("statut", "ANNULEE"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/mes/absences"))
                .andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();
        assertEquals(List.of(annulee), model.get("absences"));
        assertEquals(StatutAbsence.ANNULEE, model.get("filtreStatut"));
    }

    @Test
    void filtreLaListeAdministrativeAvecLeParametreStatutDirect() throws Exception {
        DemandeConge refusee = mock(DemandeConge.class);
        when(demandeCongeService.rechercher(any(DemandeCongeFilter.class))).thenAnswer(invocation -> {
            DemandeCongeFilter filter = invocation.getArgument(0);
            assertEquals(StatutDemandeConge.REFUSEE, filter.getStatut());
            return List.of(refusee);
        });
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(congeDemandeController).build();

        var result = mockMvc.perform(get("/conges/demandes").param("statut", "REFUSEE"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/demandes/index"))
                .andReturn();

        assertEquals(List.of(refusee), result.getModelAndView().getModel().get("demandes"));
    }

    @Test
    void filtreLaListeAdministrativeDesAbsencesAvecLeParametreStatutDirect() throws Exception {
        when(absenceService.lister(any(AbsenceFilter.class))).thenAnswer(invocation -> {
            AbsenceFilter filter = invocation.getArgument(0);
            assertEquals(StatutAbsence.ANNULEE, filter.getStatut());
            return List.of();
        });
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(absenceController).build();

        var result = mockMvc.perform(get("/conges/absences").param("statut", "ANNULEE"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/absences/index"))
                .andReturn();

        assertEquals(List.of(), result.getModelAndView().getModel().get("absences"));
    }

    @Test
    void leDetailAdministratifExposeLaBaseDuFiltreDeStatut() throws Exception {
        preparerDemandePourDetail(61);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(congeDemandeController).build();

        var result = mockMvc.perform(get("/conges/demandes/61"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/demandes/detail"))
                .andReturn();

        assertEquals("/conges/demandes", result.getModelAndView().getModel().get("filtreStatutBase"));
    }

    @Test
    void leDetailDeValidationExposeLaBaseDuFiltreDeStatut() throws Exception {
        preparerDemandePourDetail(62);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(congeValidationController).build();

        var result = mockMvc.perform(get("/conges/validations/62"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/demandes/detail"))
                .andReturn();

        assertEquals("/conges/validations", result.getModelAndView().getModel().get("filtreStatutBase"));
    }

    @Test
    void leDetailPersonnelExposeLaBaseDuFiltreDeStatut() throws Exception {
        preparerDemandePourDetail(63);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(mesCongeController).build();

        var result = mockMvc.perform(get("/mes-conges/63"))
                .andExpect(status().isOk())
                .andExpect(view().name("conges/demandes/detail"))
                .andReturn();

        assertEquals("/mes-conges/demandes", result.getModelAndView().getModel().get("filtreStatutBase"));
    }

    private void preparerDemandePourDetail(int id) {
        DemandeConge demande = mock(DemandeConge.class);
        Employe employe = mock(Employe.class);
        TypeConge typeConge = mock(TypeConge.class);
        Utilisateur utilisateur = mock(Utilisateur.class);
        when(demandeCongeService.getById(id)).thenReturn(demande);
        when(demande.getEmploye()).thenReturn(employe);
        when(demande.getTypeConge()).thenReturn(typeConge);
        when(employe.getIdEmployer()).thenReturn(42);
        when(typeConge.getId()).thenReturn(5);
        when(accessService.utilisateurCourant()).thenReturn(utilisateur);
        when(accessService.peutVoirDemande(demande, utilisateur)).thenReturn(true);
        when(soldeCongeService.rechercher(null, 42, 5)).thenReturn(List.of());
    }

    private static DemandeConge demande(StatutDemandeConge statut) {
        DemandeConge demande = mock(DemandeConge.class);
        when(demande.getStatut()).thenReturn(statut);
        return demande;
    }
}
