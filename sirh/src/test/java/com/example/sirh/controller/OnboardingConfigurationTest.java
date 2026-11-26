package com.example.sirh.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.sirh.model.ModeleOnboarding;
import com.example.sirh.model.ModeleTacheOnboarding;
import com.example.sirh.model.Role;
import com.example.sirh.model.TypeDocument;
import com.example.sirh.repository.ModeleOnboardingRepository;
import com.example.sirh.repository.ModeleTacheOnboardingRepository;
import com.example.sirh.repository.RoleRepository;
import com.example.sirh.repository.TypeDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class OnboardingConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModeleOnboardingRepository modeleRepository;
    @Autowired
    private ModeleTacheOnboardingRepository tacheRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TypeDocumentRepository typeDocumentRepository;

    @Test
    @WithMockSirhUser
    void rhPeutOuvrirLaConfigurationDuWorkflow() throws Exception {
        mockMvc.perform(get("/onboarding/modeles"))
                .andExpect(status().isOk())
                .andExpect(view().name("onboarding/modeles"));
    }

    @Test
    @WithMockSirhUser
    @Transactional
    void rhPeutOuvrirLeDetailDUnModeleAvecSesTaches() throws Exception {
        Role role = roleRepository.findAll().stream()
                .filter(candidat -> "RH".equals(candidat.getLibelle()))
                .findFirst()
                .orElseGet(() -> roleRepository.save(Role.builder().libelle("RH").build()));
        TypeDocument typeDocument = typeDocumentRepository.findAll().stream()
                .filter(candidat -> "RIB".equals(candidat.getLibelle()))
                .findFirst()
                .orElseGet(() -> typeDocumentRepository.save(TypeDocument.builder().libelle("RIB").build()));
        ModeleOnboarding modele = modeleRepository.save(
                ModeleOnboarding.builder().nom("Parcours test detail").build());
        tacheRepository.save(ModeleTacheOnboarding.builder()
                .modele(modele)
                .titre("Vérifier le dossier")
                .roleResponsable(role)
                .delaiJours(2)
                .obligatoire(true)
                .typeDocumentRequis(typeDocument)
                .ordre(1)
                .build());

        mockMvc.perform(get("/onboarding/modeles/" + modele.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("onboarding/modele-detail"));
    }

    @Test
    @WithMockSirhUser
    @Transactional
    void rhPeutCreerConfigurerEtActiverUnParcours() throws Exception {
        Role role = roleRepository.findAll().stream()
                .filter(candidat -> "RH".equals(candidat.getLibelle()))
                .findFirst()
                .orElseGet(() -> roleRepository.save(Role.builder().libelle("RH").build()));
        TypeDocument typeDocument = typeDocumentRepository.findAll().stream()
                .filter(candidat -> "RIB".equals(candidat.getLibelle()))
                .findFirst()
                .orElseGet(() -> typeDocumentRepository.save(TypeDocument.builder().libelle("RIB").build()));
        String nomModele = "Workflow configurable test";

        mockMvc.perform(post("/onboarding/modeles")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("nom", nomModele))
                .andExpect(status().is3xxRedirection());

        ModeleOnboarding modele = modeleRepository.findByNomIgnoreCase(nomModele).orElseThrow();
        mockMvc.perform(post("/onboarding/modeles/{id}/taches", modele.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("titre", "Vérifier les accès")
                        .param("idRoleResponsable", role.getIdRole().toString())
                        .param("delaiJours", "4")
                        .param("obligatoire", "false")
                        .param("idTypeDocumentRequis", typeDocument.getIdTypeDocument().toString())
                        .param("ordre", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/onboarding/modeles/" + modele.getId()));

        mockMvc.perform(post("/onboarding/modeles/{id}/activer", modele.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection());

        ModeleTacheOnboarding tache = tacheRepository.findByModele_IdOrderByOrdreAsc(modele.getId())
                .stream().findFirst().orElseThrow();
        assertEquals("Vérifier les accès", tache.getTitre());
        assertEquals(4, tache.getDelaiJours());
        assertEquals(false, tache.getObligatoire());
        assertEquals("RIB", tache.getTypeDocumentRequis().getLibelle());
        assertTrue(modeleRepository.findById(modele.getId()).orElseThrow().isActif());
        assertEquals(1, modeleRepository.findAllByActifTrue().size());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerNePeutPasAdministrerLesModelesOnboarding() throws Exception {
        mockMvc.perform(get("/onboarding/modeles"))
                .andExpect(status().isForbidden());
    }
}
