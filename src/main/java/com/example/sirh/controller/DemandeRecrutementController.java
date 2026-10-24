package com.example.sirh.controller;

import com.example.sirh.constante.ConstanteDemande;
import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.model.*;
import com.example.sirh.wrapper.DemandeRecrutementFilter;

import lombok.RequiredArgsConstructor;

import com.example.sirh.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/demandes")
@RequiredArgsConstructor 
public class DemandeRecrutementController {

        private final DemandeRecrutementRepository demandeRepository;
        private final UtilisateurRepository utilisateurRepository;
        private final PosteRepository posteRepository;
        private final DepartementRepository departementRepository;
        private final EtatDemandeRepository etatDemandeRepository;
        private final HistoriqueDemandeRecrutementRepository historiqueRepository;
        private final StatutDemandeRecrutementRepository statutRepository;


        // -----------------------------------------------------------------
        // INDEX : Liste avec pagination et filtre
        // -----------------------------------------------------------------
        @GetMapping
        public String index(
                        @ModelAttribute("filter") DemandeRecrutementFilter filter,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
                
                if (filter.getDateMin() == null)
                        filter.setDateMin(java.time.LocalDate.now());
                if (filter.getDateMax() == null)
                        filter.setDateMax(java.time.LocalDate.now());

                Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100),
                                Sort.by("dateCreation").descending());

                // APPEL SIMPLIFIÉ
                Page<DemandeRecrutementLib> demandesPage = demandeRepository.findAllLib(filter, pageable);

                model.addAttribute("demandes", demandesPage);
                model.addAttribute("postes", posteRepository.findAll());
                model.addAttribute("departements", departementRepository.findAll());
                model.addAttribute("managers", utilisateurRepository.findAll());

                return "demande/index";
        }

        // -----------------------------------------------------------------
        // CREATE (GET) : Formulaire de création
        // -----------------------------------------------------------------
        @GetMapping("/create")
        public String create(Model model) {
                model.addAttribute("postes", posteRepository.findAll());
                // On ne propose que les utilisateurs ayant le rôle MANAGER (filtrage basique en
                // Java)
                List<Utilisateur> managers = utilisateurRepository.findAll().stream()
                                .filter(u -> "MANAGER".equalsIgnoreCase(u.getRole().getLibelle()))
                                .toList();
                model.addAttribute("managers", managers);

                return "demande/form";
        }

        // -----------------------------------------------------------------
        // CREATE (POST) : Enregistrement
        // -----------------------------------------------------------------
        @PostMapping("/create")
        public String store(
                        @RequestParam Integer idManager,
                        @RequestParam Integer idPoste,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateSouhaitee,
                        @RequestParam Integer effectif,
                        @RequestParam(required = false) String motif) {

                Utilisateur manager = utilisateurRepository.findById(idManager)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                "Manager introuvable"));

                Poste poste = posteRepository.findById(idPoste)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                "Poste introuvable"));

                StatutDemandeRecrutement statutBrouillon = statutRepository.findById(ConstanteDemande.BROUILLON).get();

                DemandeRecrutement demande = DemandeRecrutement.builder()
                                .manager(manager)
                                .poste(poste)
                                .dateSouhaitee(dateSouhaitee)
                                .effectif(effectif)
                                .motif(motif)
                                .statut(statutBrouillon) 
                                .build();

                DemandeRecrutement savedDemande = demandeRepository.save(demande);

                return "redirect:/demandes/" + savedDemande.getIdDemandeRecrutement();
        }

        // -----------------------------------------------------------------
        // SHOW : Fiche de la demande avec historique d'état
        // -----------------------------------------------------------------
        @GetMapping("/{id}")
        public String show(@PathVariable Integer id, Model model) {
                DemandeRecrutement demande = demandeRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Demande introuvable"));

                List<HistoriqueDemandeRecrutement> historique = historiqueRepository.getByIdDemandeRecrutement(id);

                model.addAttribute("demande", demande);
                model.addAttribute("historique", historique);

                return "demande/show";
        }
}