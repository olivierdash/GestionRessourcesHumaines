package com.example.sirh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.example.sirh.model.DemandeConge;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.model.enums.StatutDemandeConge;
import com.example.sirh.repository.DemandeCongeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DemandeCongeServiceValidationPaginationTest {

    @Mock
    private DemandeCongeRepository demandeCongeRepository;

    @Mock
    private CongeAccessService accessService;

    @InjectMocks
    private DemandeCongeService demandeCongeService;

    @Test
    void rhSansFiltreObtientUnePageDesStatutsAutorises() {
        Utilisateur utilisateur = mock(Utilisateur.class);
        Pageable pageable = PageRequest.of(1, 10);
        List<StatutDemandeConge> statutsAutorises = List.of(
                StatutDemandeConge.SOUMISE, StatutDemandeConge.VALIDEE_MANAGER);
        Page<DemandeConge> page = new PageImpl<>(List.of(), pageable, 0);
        when(accessService.estRhOuDg(utilisateur)).thenReturn(true);
        when(demandeCongeRepository.findByStatutInOrderByDateCreationAsc(
                statutsAutorises, pageable)).thenReturn(page);

        assertEquals(page, demandeCongeService.fileValidation(utilisateur, null, pageable));
        verify(demandeCongeRepository).findByStatutInOrderByDateCreationAsc(
                statutsAutorises, pageable);
    }

    @Test
    void rhNePeutPasFiltrerLaFileSurUnStatutHorsValidation() {
        Utilisateur utilisateur = mock(Utilisateur.class);
        Pageable pageable = PageRequest.of(0, 10);
        when(accessService.estRhOuDg(utilisateur)).thenReturn(true);

        Page<DemandeConge> page = demandeCongeService.fileValidation(
                utilisateur, StatutDemandeConge.APPROUVEE, pageable);

        assertTrue(page.isEmpty());
        verifyNoInteractions(demandeCongeRepository);
    }

    @Test
    void rhAvecUnStatutAutoriseDemandeCettePagePrecisementAuRepository() {
        Utilisateur utilisateur = mock(Utilisateur.class);
        Pageable pageable = PageRequest.of(1, 10);
        Page<DemandeConge> page = new PageImpl<>(List.of(), pageable, 11);
        when(accessService.estRhOuDg(utilisateur)).thenReturn(true);
        when(demandeCongeRepository.findByStatutOrderByDateCreationAsc(
                StatutDemandeConge.VALIDEE_MANAGER, pageable)).thenReturn(page);

        assertEquals(page, demandeCongeService.fileValidation(
                utilisateur, StatutDemandeConge.VALIDEE_MANAGER, pageable));
        verify(demandeCongeRepository).findByStatutOrderByDateCreationAsc(
                StatutDemandeConge.VALIDEE_MANAGER, pageable);
    }

    @Test
    void managerSansFiltreObtientUnePageLimiteeASesDemandesSoumises() {
        Utilisateur manager = mock(Utilisateur.class);
        when(manager.getIdUtilisateur()).thenReturn(42);
        when(accessService.estRhOuDg(manager)).thenReturn(false);
        Pageable pageable = PageRequest.of(0, 10);
        Page<DemandeConge> page = new PageImpl<>(List.of(), pageable, 0);
        when(demandeCongeRepository.findByManagerAttenduIdUtilisateurAndStatutInOrderByDateCreationAsc(
                42, List.of(StatutDemandeConge.SOUMISE), pageable)).thenReturn(page);

        assertEquals(page, demandeCongeService.fileValidation(manager, null, pageable));
        verify(demandeCongeRepository).findByManagerAttenduIdUtilisateurAndStatutInOrderByDateCreationAsc(
                42, List.of(StatutDemandeConge.SOUMISE), pageable);
    }

    @Test
    void managerNePeutPasFiltrerSurLesDemandesValideesParLeManager() {
        Utilisateur manager = mock(Utilisateur.class);
        when(manager.getIdUtilisateur()).thenReturn(42);
        when(accessService.estRhOuDg(manager)).thenReturn(false);
        Pageable pageable = PageRequest.of(0, 10);

        Page<DemandeConge> page = demandeCongeService.fileValidation(
                manager, StatutDemandeConge.VALIDEE_MANAGER, pageable);

        assertTrue(page.isEmpty());
        verifyNoInteractions(demandeCongeRepository);
    }

    @Test
    void managerAvecLeStatutSoumiseDemandeUnePageLimiteeASonCompte() {
        Utilisateur manager = mock(Utilisateur.class);
        when(manager.getIdUtilisateur()).thenReturn(42);
        when(accessService.estRhOuDg(manager)).thenReturn(false);
        Pageable pageable = PageRequest.of(0, 10);
        Page<DemandeConge> page = new PageImpl<>(List.of(), pageable, 0);
        when(demandeCongeRepository.findByStatutAndManagerAttenduIdUtilisateurOrderByDateCreationAsc(
                StatutDemandeConge.SOUMISE, 42, pageable)).thenReturn(page);

        assertEquals(page, demandeCongeService.fileValidation(
                manager, StatutDemandeConge.SOUMISE, pageable));
        verify(demandeCongeRepository).findByStatutAndManagerAttenduIdUtilisateurOrderByDateCreationAsc(
                StatutDemandeConge.SOUMISE, 42, pageable);
    }
}
