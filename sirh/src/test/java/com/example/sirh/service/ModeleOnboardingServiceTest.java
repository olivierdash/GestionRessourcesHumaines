package com.example.sirh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.sirh.model.ModeleOnboarding;
import com.example.sirh.model.ModeleTacheOnboarding;
import com.example.sirh.model.Role;
import com.example.sirh.model.TypeDocument;
import com.example.sirh.repository.ModeleOnboardingRepository;
import com.example.sirh.repository.ModeleTacheOnboardingRepository;
import com.example.sirh.repository.RoleRepository;
import com.example.sirh.repository.TypeDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModeleOnboardingServiceTest {

    @Mock
    private ModeleOnboardingRepository modeleRepository;
    @Mock
    private ModeleTacheOnboardingRepository tacheRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TypeDocumentRepository typeDocumentRepository;

    @InjectMocks
    private ModeleOnboardingService service;

    @Test
    void activationRefuseUnModeleSansTache() {
        ModeleOnboarding modele = ModeleOnboarding.builder().id(4).nom("Vide").build();
        when(modeleRepository.findById(4)).thenReturn(Optional.of(modele));
        when(tacheRepository.findByModele_IdOrderByOrdreAsc(4)).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> service.activer(4));

        verify(modeleRepository, never()).save(any(ModeleOnboarding.class));
    }

    @Test
    void activationDesactiveLeModelePrecedent() {
        ModeleOnboarding ancien = ModeleOnboarding.builder().id(1).nom("Ancien").actif(true).build();
        ModeleOnboarding nouveau = ModeleOnboarding.builder().id(2).nom("Nouveau").actif(false).build();
        ModeleTacheOnboarding tache = ModeleTacheOnboarding.builder().id(3).modele(nouveau).build();
        when(modeleRepository.findById(2)).thenReturn(Optional.of(nouveau));
        when(modeleRepository.findAllByActifTrue()).thenReturn(List.of(ancien));
        when(tacheRepository.findByModele_IdOrderByOrdreAsc(2)).thenReturn(List.of(tache));

        service.activer(2);

        assertFalse(ancien.isActif());
        assertTrue(nouveau.isActif());
        verify(modeleRepository).flush();
        verify(modeleRepository).save(nouveau);
    }

    @Test
    void ajouterTacheAssocieLeRoleEtLeDocumentChoisis() {
        ModeleOnboarding modele = ModeleOnboarding.builder().id(4).nom("Accueil").build();
        Role role = Role.builder().idRole(7).libelle("RH").build();
        TypeDocument document = TypeDocument.builder().idTypeDocument(9).libelle("RIB").build();
        when(modeleRepository.findById(4)).thenReturn(Optional.of(modele));
        when(roleRepository.findById(7)).thenReturn(Optional.of(role));
        when(typeDocumentRepository.findById(9)).thenReturn(Optional.of(document));
        when(tacheRepository.save(any(ModeleTacheOnboarding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ModeleTacheOnboarding creee = service.ajouterTache(4, "Contrôler le dossier", 7,
                6, false, 9, 1);

        assertEquals("Contrôler le dossier", creee.getTitre());
        assertEquals(6, creee.getDelaiJours());
        assertEquals(false, creee.getObligatoire());
        assertEquals(1, creee.getOrdre());
        assertSame(role, creee.getRoleResponsable());
        assertSame(document, creee.getTypeDocumentRequis());
    }
}
