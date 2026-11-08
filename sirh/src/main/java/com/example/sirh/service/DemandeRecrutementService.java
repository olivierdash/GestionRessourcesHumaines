package com.example.sirh.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sirh.dto.DemandeRecrutementDTO;
import com.example.sirh.dto.DemandeRecrutementFilter;
import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.dto.ValidationPageData;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.DemandeRecrutement;
import com.example.sirh.model.EtatDemande;
import com.example.sirh.model.GrilleSalarial;
import com.example.sirh.model.HistoriqueDemandeRecrutement;
import com.example.sirh.model.MotifDemande;
import com.example.sirh.model.Poste;
import com.example.sirh.model.TypeContrat;
import com.example.sirh.model.Utilisateur;
import com.example.sirh.repository.DemandeRecrutementRepository;

import lombok.AllArgsConstructor;

import static com.example.sirh.constants.ConstantSirh.Action;
import static com.example.sirh.constants.ConstantSirh.Role;
import static com.example.sirh.constants.ConstantSirh.Statut;

@Service
@AllArgsConstructor
public class DemandeRecrutementService {

    private final UtilisateurService utilisateurService;
    private final PosteService posteService;
    private final MotifDemandeService motifDemandeService;
    private final TypeContratService typeContratService;
    private final DemandeRecrutementRepository demandeRepository;
    private final EtatDemandeService etatDemandeService;
    private final HistoriqueDemandeRecrutementService historiqueService;
    private final GrilleSalarialService grilleSalarialService;

    private void validerChampsObligatoires(DemandeRecrutementDTO dto, Map<String, String> errors) {
        if (dto.getIdManager() == null) {
            errors.put("idManager", "Le manager est obligatoire pour créer une demande.");
        }
        if (dto.getIdPoste() == null) {
            errors.put("idPoste", "Le poste concerné est obligatoire.");
        }
        if (dto.getIdTypeContrat() == null) {
            errors.put("idTypeContrat", "Le type de contrat est obligatoire.");
        }
        if (dto.getMotif() == null || dto.getMotif().getIdMotifDemande() == null) {
            errors.put("motif", "Le motif de la demande est obligatoire.");
        }
    }

    private void validerReglesMetier(DemandeRecrutementDTO dto, Map<String, String> errors, Poste poste) {
        if (poste != null && dto.getDateSouhaitee() != null
                && !errors.containsKey("idPoste") && !errors.containsKey("dateSouhaitee")) {

            List<String> etatsFinaux = List.of(Statut.VALIDE, Statut.REFUSE_DEMANDE);
            boolean existeDeja = demandeRepository.existsDemandeActive(
                    poste.getIdPoste(),
                    etatsFinaux);

            if (existeDeja) {
                errors.put("idPoste", "Une demande active existe déjà pour ce poste, ou pour cette date dans ce département.");
            }
        }

        if (dto.getDateSouhaitee() == null) {
            errors.put("dateSouhaitee", "La date d'entrée est obligatoire.");
        } else if (dto.getDateSouhaitee().isBefore(LocalDate.now())) {
            errors.put("dateSouhaitee", "La date doit être aujourd'hui ou dans le futur.");
        }

        if (dto.getEffectif() == null || dto.getEffectif() <= 0) {
            errors.put("effectif", "L'effectif doit être un nombre positif.");
        }
    }

    @Transactional
    public DemandeRecrutement createDemande(DemandeRecrutementDTO dto, String action) {
        Map<String, String> errors = new LinkedHashMap<>();

        validerChampsObligatoires(dto, errors);

        Poste poste = null;
        if (dto.getIdPoste() != null && !errors.containsKey("idPoste")) {
            poste = posteService.getById(dto.getIdPoste());
        }

        if (Action.SUBMIT.equals(action)) {
            validerReglesMetier(dto, errors, poste);
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        Utilisateur manager = utilisateurService.getById(dto.getIdManager());
        if (poste == null) {
            poste = posteService.getById(dto.getIdPoste());
        }
        MotifDemande motif = motifDemandeService.getById(dto.getMotif().getIdMotifDemande());
        TypeContrat typeContrat = typeContratService.getById(dto.getIdTypeContrat());

        String intituleEtat = Action.SUBMIT.equals(action) ? Statut.CREE : Statut.BROUILLON_DEMANDE;
        EtatDemande etatCible = etatDemandeService.getByIntitule(intituleEtat);

        DemandeRecrutement demande = DemandeRecrutement.builder()
                .manager(manager)
                .poste(poste)
                .typeContrat(typeContrat)
                .dateSouhaitee(dto.getDateSouhaitee())
                .effectif(dto.getEffectif())
                .motif(motif)
                .etatDemande(etatCible)
                .commentaires(dto.getRemarque())
                .build();

        return demandeRepository.save(demande);
    }

    @Transactional
    public DemandeRecrutement updateDemande(DemandeRecrutementDTO dto, Utilisateur validateur, String action) {
        DemandeRecrutement demande = getById(dto.getIdDemandeRecrutement());
        Map<String, String> errors = new LinkedHashMap<>();

        String etatActuel = demande.getEtatDemande().getIntitule();
        String role = validateur.getRole().getLibelle();

        if (!Statut.BROUILLON_DEMANDE.equalsIgnoreCase(etatActuel)
                || !Role.MANAGER.equalsIgnoreCase(role)) {
            errors.put("global", "Modification Impossible : seul le manager peut modifier un brouillon.");
            throw new ValidationMetierException(errors);
        }

        validerChampsObligatoires(dto, errors);

        Poste poste = null;
        if (dto.getIdPoste() != null && !errors.containsKey("idPoste")) {
            poste = posteService.getById(dto.getIdPoste());
        }

        if (Action.SUBMIT.equals(action)) {
            validerReglesMetier(dto, errors, poste);
        }

        if (!errors.isEmpty()) {
            throw new ValidationMetierException(errors);
        }

        Utilisateur manager = utilisateurService.getById(dto.getIdManager());
        if (poste == null) {
            poste = posteService.getById(dto.getIdPoste());
        }
        MotifDemande motif = motifDemandeService.getById(dto.getMotif().getIdMotifDemande());
        TypeContrat typeContrat = typeContratService.getById(dto.getIdTypeContrat());

        demande.setManager(manager);
        demande.setPoste(poste);
        demande.setTypeContrat(typeContrat);
        demande.setDateSouhaitee(dto.getDateSouhaitee());
        demande.setEffectif(dto.getEffectif());
        demande.setMotif(motif);
        demande.setCommentaires(dto.getRemarque());

        if (Action.SUBMIT.equals(action)) {
            EtatDemande nouvelEtat = etatDemandeService.getByIntitule(Statut.CREE);
            demande.setEtatDemande(nouvelEtat);
            demandeRepository.save(demande);
            saveHistorique(demande, validateur);
        } else {
            demandeRepository.save(demande);
        }

        return demande;
    }

    public DemandeRecrutement getById(Integer id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Demande introuvable"));
    }

    public List<DemandeRecrutement> getAllDemandeValide(DemandeRecrutementFilter filter) {
        return demandeRepository.getAllDemandeValide(filter);
    }

    @Transactional
    public DemandeRecrutement updateStateDemande(Integer idDemande, Utilisateur validateur, String remarque, String action) {
        DemandeRecrutement demande = getById(idDemande);
        demande.setCommentaires(remarque);

        String etatActuel = demande.getEtatDemande().getIntitule();
        String role = validateur.getRole().getLibelle();

        EtatDemande nouvelEtat = null;

        if (Action.REFUSER.equals(action)) {
            if (Role.RH.equalsIgnoreCase(role)
                    || Role.FINANCE.equalsIgnoreCase(role)
                    || Role.DG.equalsIgnoreCase(role)) {
                nouvelEtat = etatDemandeService.getByIntitule(Statut.REFUSE_DEMANDE);
            } else {
                throw new RuntimeException("Seul un validateur peut refuser une demande.");
            }
        } else {
            if (Statut.CREE.equalsIgnoreCase(etatActuel) && Role.RH.equalsIgnoreCase(role)) {
                nouvelEtat = etatDemandeService.getByIntitule(Statut.VALIDE_RH);
            } else if (Statut.VALIDE_RH.equalsIgnoreCase(etatActuel) && Role.FINANCE.equalsIgnoreCase(role)) {
                nouvelEtat = etatDemandeService.getByIntitule(Statut.VALIDE_FINANCE);
            } else if (Statut.VALIDE_FINANCE.equalsIgnoreCase(etatActuel) && Role.DG.equalsIgnoreCase(role)) {
                nouvelEtat = etatDemandeService.getByIntitule(Statut.VALIDE);
            }
        }

        if (nouvelEtat == null) {
            throw new RuntimeException("Action impossible pour cet état ou ce rôle.");
        }

        demande.setEtatDemande(nouvelEtat);
        demandeRepository.save(demande);
        saveHistorique(demande, validateur);

        return demande;
    }

    @Transactional(readOnly = true)
    public Page<DemandeRecrutementLib> findAllLib(DemandeRecrutementFilter filter, Pageable pageable) {
        return demandeRepository.findAllLib(filter, pageable);
    }

    public DemandeRecrutementDTO getDtoForEdit(Integer id) {
        DemandeRecrutement demande = getById(id);
        if (!Statut.BROUILLON_DEMANDE.equalsIgnoreCase(demande.getEtatDemande().getIntitule())) {
            throw new RuntimeException("Cette demande n'est plus modifiable.");
        }
        return toDto(demande);
    }

    private DemandeRecrutementDTO toDto(DemandeRecrutement demande) {
        if (demande == null) return null;
        DemandeRecrutementDTO dto = new DemandeRecrutementDTO();
        dto.setIdDemandeRecrutement(demande.getIdDemandeRecrutement());
        if (demande.getManager() != null) dto.setIdManager(demande.getManager().getIdUtilisateur());
        if (demande.getPoste() != null) dto.setIdPoste(demande.getPoste().getIdPoste());
        if (demande.getTypeContrat() != null) dto.setIdTypeContrat(demande.getTypeContrat().getId());
        dto.setDateSouhaitee(demande.getDateSouhaitee());
        dto.setEffectif(demande.getEffectif());
        if (demande.getMotif() != null) dto.setMotif(demande.getMotif());
        if (demande.getEtatDemande() != null) {
            dto.setIdEtat(demande.getEtatDemande().getIdEtatDemande());
            dto.setEtat(demande.getEtatDemande().getIntitule());
        }
        dto.setRemarque(demande.getCommentaires());
        return dto;
    }

    public boolean canValidate(String etat, String role) {
        return (Statut.CREE.equalsIgnoreCase(etat) && Role.RH.equalsIgnoreCase(role))
                || (Statut.VALIDE_RH.equalsIgnoreCase(etat) && Role.FINANCE.equalsIgnoreCase(role))
                || (Statut.VALIDE_FINANCE.equalsIgnoreCase(etat) && Role.DG.equalsIgnoreCase(role));
    }

    @Transactional(readOnly = true)
    public ValidationPageData getValidationPageData(String role) {
        String etatRequis = switch (role) {
            case Role.RH -> Statut.CREE;
            case Role.FINANCE -> Statut.VALIDE_RH;
            case Role.DG -> Statut.VALIDE_FINANCE;
            default -> null;
        };

        List<DemandeRecrutement> demandes = (etatRequis != null)
                ? demandeRepository.findByEtatDemandeIntituleIgnoreCaseOrderByDateCreationDesc(etatRequis)
                : List.of();

        Map<Integer, GrilleSalarial> grilles = new HashMap<>();

        if (Role.FINANCE.equals(role)) {
            for (DemandeRecrutement d : demandes) {
                grilleSalarialService.getGrillePourPosteEtDepartement(
                        d.getPoste().getIdPoste(),
                        d.getPoste().getDepartement().getIdDepartement())
                        .ifPresent(grille -> grilles.put(d.getIdDemandeRecrutement(), grille));
            }
        }
        return new ValidationPageData(demandes, grilles);
    }

    @Transactional
    public void saveHistorique(DemandeRecrutement demande, Utilisateur validateur) {
        HistoriqueDemandeRecrutement histo = new HistoriqueDemandeRecrutement();
        histo.setCommentaires(demande.getCommentaires());
        histo.setDemandeRecrutement(demande);
        histo.setEtatDemande(demande.getEtatDemande());
        histo.setValidateur(validateur);
        historiqueService.save(histo);
    }
}