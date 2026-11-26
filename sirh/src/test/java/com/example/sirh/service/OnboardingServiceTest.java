package com.example.sirh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.sirh.model.Employe;
import com.example.sirh.model.ModeleOnboarding;
import com.example.sirh.model.ModeleTacheOnboarding;
import com.example.sirh.model.Onboarding;
import com.example.sirh.model.OnboardingTache;
import com.example.sirh.model.Role;
import com.example.sirh.model.StatutTacheOnboarding;
import com.example.sirh.model.TypeDocument;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.ModeleOnboardingRepository;
import com.example.sirh.repository.ModeleTacheOnboardingRepository;
import com.example.sirh.repository.OnboardingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private OnboardingRepository onboardingRepository;
    @Mock
    private ModeleOnboardingRepository modeleOnboardingRepository;
    @Mock
    private ModeleTacheOnboardingRepository modeleTacheOnboardingRepository;
    @Mock
    private OnboardingTacheService onboardingTacheService;
    @Mock
    private StatutTacheOnboardingService statutTacheOnboardingService;
    @Mock
    private UtilisateurService utilisateurService;
    @Mock
    private EmployeService employeService;
    @Mock
    private DocumentService documentService;
    @Mock
    private StatutEmployeService statutEmployeService;

    @InjectMocks
    private OnboardingService service;

    @Test
    void creerCopieLesTachesDuModeleActif() {
        Employe employe = Employe.builder().idEmployer(10).build();
        ModeleOnboarding modele = ModeleOnboarding.builder()
                .id(4).nom("Accueil standard").actif(true).build();
        TypeDocument documentRequis = TypeDocument.builder()
                .idTypeDocument(2).libelle("RIB").build();
        Role role = Role.builder().idRole(1).libelle("RH").build();
        ModeleTacheOnboarding definition = ModeleTacheOnboarding.builder()
                .id(8).modele(modele).titre("Préparer le poste de travail")
                .roleResponsable(role).delaiJours(5).obligatoire(false)
                .typeDocumentRequis(documentRequis).ordre(2).build();
        Utilisateur responsable = Utilisateur.builder().idUtilisateur(3).build();
        StatutTacheOnboarding aFaire = new StatutTacheOnboarding();

        when(onboardingRepository.findByEmployeIdEmployer(10)).thenReturn(Optional.empty());
        when(onboardingRepository.save(any(Onboarding.class))).thenAnswer(invocation -> {
            Onboarding onboarding = invocation.getArgument(0);
            onboarding.setId(12);
            return onboarding;
        });
        when(modeleOnboardingRepository.findByActifTrue()).thenReturn(Optional.of(modele));
        when(modeleTacheOnboardingRepository.findByModele_IdOrderByOrdreAsc(4))
                .thenReturn(List.of(definition));
        when(utilisateurService.findFirstByRoleLibelle("RH")).thenReturn(responsable);
        when(statutTacheOnboardingService.getByLibelle("à faire")).thenReturn(aFaire);

        service.creer(employe);

        ArgumentCaptor<OnboardingTache> captor = ArgumentCaptor.forClass(OnboardingTache.class);
        verify(onboardingTacheService, times(1)).save(captor.capture());
        OnboardingTache created = captor.getValue();
        assertEquals("Préparer le poste de travail", created.getTitre());
        assertEquals(2, created.getOrdre());
        assertEquals(LocalDate.now().plusDays(5), created.getDateEcheance());
        assertEquals(false, created.getObligatoire());
        assertSame(documentRequis, created.getTypeDocumentRequis());
        assertSame(responsable, created.getResponsable());
    }

    @Test
    void terminerTacheExigeLeDocumentConfigureQuelQueSoitLeTitre() {
        TypeDocument documentRequis = TypeDocument.builder()
                .idTypeDocument(2).libelle("RIB").build();
        Onboarding onboarding = new Onboarding();
        onboarding.setId(12);
        OnboardingTache tache = new OnboardingTache();
        tache.setId(31);
        tache.setTitre("Préparer le poste de travail");
        tache.setOnboarding(onboarding);
        tache.setTypeDocumentRequis(documentRequis);
        tache.setObligatoire(true);
        when(onboardingTacheService.getById(31)).thenReturn(tache);
        when(documentService.existsByTacheIdAndTypeDocumentLibelle(31, "RIB")).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> service.terminerTache(31));

        verify(documentService).existsByTacheIdAndTypeDocumentLibelle(31, "RIB");
        verify(onboardingTacheService, never()).saveAndFlush(any(OnboardingTache.class));
    }
}
