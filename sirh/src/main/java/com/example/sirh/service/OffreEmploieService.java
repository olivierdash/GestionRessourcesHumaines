package com.example.sirh.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.sirh.dto.OffreFilter;
import com.example.sirh.dto.OffrePublicDto;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.OffreEmploi;
import com.example.sirh.model.StatutOffreEmploi;
import com.example.sirh.repository.OffreEmploieRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * @author vox
 *         OffreEmploieService
 */
@Service
@AllArgsConstructor
public class OffreEmploieService {

    private final OffreEmploieRepository offreEmploieRepo;

    private final StatutOffreEmploieService statutOffreService;
    private final DemandeRecrutementService demandeRecrutementService;

    /**
     * task 8 : lister toutes les offres d'emploi
     */
    public List<OffreEmploi> listAllEmploie() {
        return offreEmploieRepo.findAllByOrderByDatePublicationDesc();
    }

    /**
     * task 9 : créer une offre d'emploi
     */

    @Transactional
    public OffreEmploi createNewOffer(OffreEmploi offre, Integer idStatutOffreEmploi) {

        Map<String, String> errors = new LinkedHashMap<>();

        // 1. Vérifier la demande liée
        if (offre.getDemandeRecrutement() == null
                || offre.getDemandeRecrutement().getIdDemandeRecrutement() == null) {
            errors.put("demandeRecrutement.idDemandeRecrutement",
                    "La demande de recrutement est obligatoire.");
        }

        // 2. Vérifier le titre
        if (offre.getTitre() == null || offre.getTitre().isBlank()) {
            errors.put("titre", "Le titre de l'offre est obligatoire.");
        }

        // 3. Vérifier la description
        if (offre.getDescription() == null || offre.getDescription().isBlank()) {
            errors.put("description", "La description de l'offre est obligatoire.");
        }

        // 4. Vérifier les dates
        if (offre.getDatePublication() == null) {
            errors.put("datePublication", "La date de début de publication est obligatoire.");
        }

        if (offre.getDateCloture() == null) {
            errors.put("dateCloture", "La date de clôture est obligatoire.");
        }

        if (offre.getDatePublication() != null && offre.getDateCloture() != null
                && !offre.getDateCloture().isAfter(offre.getDatePublication())) {
            errors.put("dateCloture",
                    "La date de clôture doit être postérieure à la date de publication.");
        }

        // 5. Vérifier le statut demandé : uniquement BROUILLON ou PUBLIEE
        if (idStatutOffreEmploi == null
                || (idStatutOffreEmploi != 1 && idStatutOffreEmploi != 2)) {
            errors.put("statut", "Le statut doit être BROUILLON ou PUBLIEE.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        // 6. Récupérer la demande depuis la base
        var demande = demandeRecrutementService.getById(
                offre.getDemandeRecrutement().getIdDemandeRecrutement());

        // 7. Vérifier la validation finale par le DG
        String etatDemande = demande.getEtatDemande().getIntitule();

        if (!"Valide".equalsIgnoreCase(etatDemande)) {
            errors.put("demandeRecrutement.idDemandeRecrutement",
                    "La demande doit être validée par le DG avant la création de l'offre.");
            throw new ValidationMetierException(errors);
        }

        // 8. Affecter les relations et le statut
        offre.setIdOffre(null);
        offre.setDemandeRecrutement(demande);
        offre.setStatut(statutOffreService.getById(idStatutOffreEmploi));

        // Conserver les dates saisies par RH
        return offreEmploieRepo.save(offre);
    }


    /**
     * task 9 bis: publier une offre
     */

    @Transactional
    public void publicationOffre(Integer idOffre) {
        OffreEmploi offre = getOffreEmploiById(idOffre);

        if (!"BROUILLON".equalsIgnoreCase(
                offre.getStatut().getLibelle())) {
            throw new RuntimeException(
                    "Seule une offre en brouillon peut être publiée."
            );
        }

        if (offre.getDatePublication() == null
                || offre.getDateCloture() == null
                || !offre.getDatePublication().isBefore(offre.getDateCloture())) {
            throw new RuntimeException("Les dates de publication sont invalides.");
        }

        if (!LocalDateTime.now().isBefore(offre.getDateCloture())) {
            throw new RuntimeException("La date de clôture est déjà dépassée.");
        }

        offre.setStatut(statutOffreService.getByLibelle("PUBLIEE"));

        // Ne pas modifier datePublication ni dateCloture.
        offreEmploieRepo.save(offre);
    }

    /**
     * correction d'une offre
     */
    public OffreEmploi getOffreEmploiById(Integer id_offre) {
        return offreEmploieRepo.findById(id_offre)
                .orElseThrow(() -> new RuntimeException("Offre introuvable"));
    }

    @Transactional
    public void updateoffer(Integer idOffre, String nouveauTitre, String nouvelleDescription) {
        OffreEmploi offre = getOffreEmploiById(idOffre);
        if ("CLOTUREE".equalsIgnoreCase(offre.getStatut().getLibelle())) {
            throw new RuntimeException("Impossible de modifier une offre déjà clôturée.");
        }
        offre.setTitre(nouveauTitre);
        offre.setDescription(nouvelleDescription);
        offreEmploieRepo.save(offre);
    }

    /**
     * expiration après date_cloture de l'offre
     */
    @Transactional
    public void expireoffer(Integer id_offre) {
        OffreEmploi offer = this.getOffreEmploiById(id_offre);
        if (offer.getDateCloture() != null && LocalDateTime.now().isAfter(offer.getDateCloture())) {
            StatutOffreEmploi statutCloturee = statutOffreService.getByLibelle("CLOTUREE");
            offer.setStatut(statutCloturee);
            offreEmploieRepo.save(offer);
        }
    }

    /**
     * Méthode générique pour récupérer les offres par statut
     */
    public List<OffreEmploi> getOffres(StatutOffreEmploi statut) {
        if (statut == null) {
            return offreEmploieRepo.findAllByOrderByDatePublicationDesc();
        }
        return offreEmploieRepo.findByStatutId(statut.getId());
    }

    /**
     * Méthode spécifique au site public : récupérer uniquement les offres PUBLIEES
     */
    public List<OffrePublicDto> getOffresPubliees() {
        return offreEmploieRepo.findOffresPubliquesActives(LocalDateTime.now())
                .stream()
                .map(this::toPublicDto)
                .collect(Collectors.toList());
    }

    /**
     * Détail d'une offre publique (uniquement si elle est PUBLIEE)
     */
    public OffrePublicDto getOffrePubliqueById(Integer id) {

        OffreEmploi offre = getOffreEmploiById(id);
        LocalDateTime maintenant = LocalDateTime.now();

        if (!"PUBLIEE".equalsIgnoreCase(offre.getStatut().getLibelle())
                || offre.getDatePublication() == null
                || offre.getDateCloture() == null
                || offre.getDatePublication().isAfter(maintenant)
                || !offre.getDateCloture().isAfter(maintenant)) {

            throw new RuntimeException(
                    "Cette offre n'est pas disponible actuellement.");
        }
        return toPublicDto(offre);
    }

    public Page<OffreEmploi> getOffresFiltrees(OffreFilter filter, Pageable pageable) {
        return offreEmploieRepo.findWithFilters(filter, pageable);
    }

    /**
     * Méthode utilitaire de conversion Entité -> DTO Public.
     * Enrichie pour alimenter JobCard / JobDetail côté Vue.
     */
    private OffrePublicDto toPublicDto(OffreEmploi offre) {
        var demande = offre.getDemandeRecrutement();
        var poste = demande.getPoste();

        return OffrePublicDto.builder()
                .id(offre.getIdOffre())
                .titre(offre.getTitre())
                .description(offre.getDescription())
                .statut(offre.getStatut().getLibelle())
                .intitulePoste(poste.getIntitule())
                .departement(poste.getDepartement().getTitre())
                .dureeHebdo(poste.getDureeHebdo())
                .effectif(demande.getEffectif())
                .dateSouhaitee(demande.getDateSouhaitee())
                .motif(demande.getMotif() != null ? demande.getMotif().getLibelle() : null)
                .datePublication(offre.getDatePublication())
                .dateCloture(offre.getDateCloture())
                .build();
    }
    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void cloturerOffresExpirees() {

        StatutOffreEmploi statutCloturee =
                statutOffreService.getByLibelle("CLOTUREE");

        int nombre = offreEmploieRepo.cloturerOffresExpirees(
                statutCloturee,
                LocalDateTime.now());

        if (nombre > 0) {
            System.out.println(nombre + " offre(s) automatiquement clôturée(s).");
        }
    }
}