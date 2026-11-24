package com.example.sirh.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Map;

import com.example.sirh.model.RuptureContrat;
import com.example.sirh.repository.ContratRepository;
import com.example.sirh.repository.EmployeRepository;
import com.example.sirh.service.AuditService;
import com.example.sirh.service.CongeAccessService;
import com.example.sirh.service.DesactivationAccesService;
import com.example.sirh.service.DocumentSortieService;
import com.example.sirh.service.MaterielService;
import com.example.sirh.service.OffboardingService;
import com.example.sirh.service.RuptureContratService;
import com.example.sirh.service.SoldeToutCompteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class OffboardingStatutFiltreControllerTest {

    @Mock
    private RuptureContratService ruptureService;
    @Mock
    private OffboardingService offboardingService;
    @Mock
    private MaterielService materielService;
    @Mock
    private DesactivationAccesService accesService;
    @Mock
    private DocumentSortieService documentService;
    @Mock
    private SoldeToutCompteService stcService;
    @Mock
    private EmployeRepository employeRepository;
    @Mock
    private ContratRepository contratRepository;
    @Mock
    private CongeAccessService accessService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private OffboardingController controller;

    @Test
    void filtreLesRupturesActivesParStatut() throws Exception {
        RuptureContrat initiee = mock(RuptureContrat.class);
        RuptureContrat cloturee = mock(RuptureContrat.class);
        lenient().when(initiee.getStatutRupture()).thenReturn("INITIEE");
        lenient().when(cloturee.getStatutRupture()).thenReturn("CLOTUREE");
        when(ruptureService.listerActifs()).thenReturn(List.of(initiee, cloturee));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        var result = mockMvc.perform(get("/offboarding").param("statut", "CLOTUREE"))
                .andExpect(status().isOk())
                .andExpect(view().name("offboarding/index"))
                .andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();
        assertEquals(List.of(cloturee), model.get("ruptures"));
        assertEquals("CLOTUREE", model.get("statutFiltre"));
    }
}
