package com.example.sirh.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.sirh.model.Contrat;
import com.example.sirh.model.Employe;
import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.model.Poste;
import com.example.sirh.model.StatutContrat;
import com.example.sirh.model.TypeContrat;
import com.example.sirh.repository.ContratRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContratServiceTest {

    @Mock
    ContratRepository contratRepository;
    @Mock
    GrilleSalarialService grilleSalarialService;
    @Mock
    StatutContratService statutContratService;
    @Mock
    EmployeService employeService;
    @Mock
    PosteService posteService;
    @Mock
    TypeContratService typeContratService;
    @Mock
    AffectationService affectationService;

    @InjectMocks
    ContratService service;

    private Employe employe;
    private Poste poste1;
    private Poste poste2;
    private StatutContrat statutActif;
    private StatutContrat statutTermine;
    private TypeContrat cdi; // pas de durée max
    private TypeContrat cdd; // 12 mois max

    @BeforeEach
    void preparer() {
        employe = Employe.builder().idEmployer(1).build();

        poste1 = mock(Poste.class);
        when(poste1.getIdPoste()).thenReturn(1);
        poste2 = mock(Poste.class);
        when(poste2.getIdPoste()).thenReturn(2);

        statutActif = mock(StatutContrat.class);
        statutTermine = mock(StatutContrat.class);
        when(statutContratService.getByLibelle("actif")).thenReturn(statutActif);
        when(statutContratService.getByLibelle("terminé")).thenReturn(statutTermine);

        cdi = TypeContrat.builder().id(1).libelle("CDI").dureeMaxMois(null).build();
        cdd = TypeContrat.builder().id(2).libelle("CDD").dureeMaxMois(12).build();

        // Grille : 1000 à 2000 pour les deux postes
        GrilleSalarial grille = mock(GrilleSalarial.class);
        when(grille.getSalaireMin()).thenReturn(new BigDecimal("1000"));
        when(grille.getSalaireMax()).thenReturn(new BigDecimal("2000"));
        when(grilleSalarialService.getGrillePourPoste(any())).thenReturn(Optional.of(grille));

        when(contratRepository.existeChevauchement(any(), any(), any())).thenReturn(false);
        when(contratRepository.save(any(Contrat.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Contrat contrat(TypeContrat type, Poste poste, LocalDate debut, LocalDate fin, String salaire) {
        return Contrat.builder()
                .employe(employe).typeContrat(type).poste(poste)
                .dateDebut(debut).dateFin(fin)
                .salaireMensuel(new BigDecimal(salaire))
                .build();
    }

    // ---------- Création ----------

    @Test
    void creer_contratValide_estActif() {
        Contrat c = contrat(cdi, poste1, LocalDate.of(2026, 12, 1), null, "1500");

        Contrat resultat = service.creer(c);

        assertSame(statutActif, resultat.getStatut());
        verify(contratRepository).save(c);
    }

    @Test
    void creer_refuseSalaireSousLaGrille() {
        Contrat c = contrat(cdi, poste1, LocalDate.of(2026, 12, 1), null, "500");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.creer(c));
        assertTrue(e.getMessage().contains("Le salaire doit être compris"));
        verify(contratRepository, never()).save(any());
    }

    @Test
    void creer_refuseSalaireAuDessusDeLaGrille() {
        Contrat c = contrat(cdi, poste1, LocalDate.of(2026, 12, 1), null, "5000");

        assertThrows(IllegalArgumentException.class, () -> service.creer(c));
        verify(contratRepository, never()).save(any());
    }

    @Test
    void creer_refuseCddSansDateDeFin() {
        Contrat c = contrat(cdd, poste1, LocalDate.of(2026, 12, 1), null, "1500");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.creer(c));
        assertTrue(e.getMessage().contains("obligatoire"));
    }

    @Test
    void creer_refuseCddTropLong() {
        Contrat c = contrat(cdd, poste1, LocalDate.of(2026, 12, 1), LocalDate.of(2028, 12, 1), "1500");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.creer(c));
        assertTrue(e.getMessage().contains("ne peut pas dépasser"));
    }

    @Test
    void creer_refuseDateDeFinAvantDebut() {
        Contrat c = contrat(cdd, poste1, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1), "1500");

        assertThrows(IllegalArgumentException.class, () -> service.creer(c));
    }

    @Test
    void creer_refuseChevauchement() {
        when(contratRepository.existeChevauchement(any(), any(), any())).thenReturn(true);
        Contrat c = contrat(cdi, poste1, LocalDate.of(2026, 12, 1), null, "1500");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> service.creer(c));
        assertTrue(e.getMessage().contains("chevauche"));
        verify(contratRepository, never()).save(any());
    }

    // ---------- Renouvellement ----------

    private Contrat ancienContrat(Poste poste) {
        Contrat ancien = contrat(cdd, poste, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "1500");
        when(contratRepository.findById(10)).thenReturn(Optional.of(ancien));
        when(contratRepository.saveAndFlush(any(Contrat.class))).thenAnswer(i -> i.getArgument(0));
        return ancien;
    }

    @Test
    void renouveler_clotureAncienContratLaVeilleDuNouveau() {
        Contrat ancien = ancienContrat(poste1);
        LocalDate debut = LocalDate.of(2027, 1, 1);
        Contrat nouveau = contrat(cdi, poste1, debut, null, "1600");

        service.renouveler(10, nouveau);

        assertEquals(debut.minusDays(1), ancien.getDateFin());
        assertSame(statutTermine, ancien.getStatut());
        assertSame(statutActif, nouveau.getStatut());
    }

    @Test
    void renouveler_memePoste_neChangePasLAffectation() {
        ancienContrat(poste1);
        Contrat nouveau = contrat(cdi, poste1, LocalDate.of(2027, 1, 1), null, "1600");

        service.renouveler(10, nouveau);

        verify(affectationService, never()).changerPoste(any(), any(), any());
    }

    @Test
    void renouveler_autrePoste_changeLAffectation() {
        ancienContrat(poste1);
        LocalDate debut = LocalDate.of(2027, 1, 1);
        Contrat nouveau = contrat(cdi, poste2, debut, null, "1600");

        service.renouveler(10, nouveau);

        verify(affectationService).changerPoste(employe, poste2, debut);
    }

    @Test
    void renouveler_salaireHorsGrille_neChangePasLAffectation() {
        ancienContrat(poste1);
        Contrat nouveau = contrat(cdi, poste2, LocalDate.of(2027, 1, 1), null, "9999");

        assertThrows(IllegalArgumentException.class, () -> service.renouveler(10, nouveau));
        verify(affectationService, never()).changerPoste(any(), any(), any());
    }

    @Test
    void renouveler_avecTrou_neProlongePasLAncienContrat() {
        Contrat ancien = contrat(cdd, poste1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), "1500");
        when(contratRepository.findById(10)).thenReturn(Optional.of(ancien));
        when(contratRepository.saveAndFlush(any(Contrat.class))).thenAnswer(i -> i.getArgument(0));
        Contrat nouveau = contrat(cdi, poste1, LocalDate.of(2026, 7, 15), null, "1600");

        service.renouveler(10, nouveau);

        assertEquals(LocalDate.of(2026, 6, 30), ancien.getDateFin());
    }
}