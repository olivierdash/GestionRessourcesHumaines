package com.example.sirh.controller;

import com.example.sirh.config.CustomUserDetails;
import com.example.sirh.dto.DemandeRecrutementDTO;
import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.model.*;
import com.example.sirh.wrapper.DemandeRecrutementFilter;
import com.example.sirh.repository.*;
import com.example.sirh.service.DemandeRecrutementService;
import com.example.sirh.service.MotifDemandeService;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/demandes")
@AllArgsConstructor
public class DemandeRecrutementController {

        private DemandeRecrutementRepository demandeRepository;
        private UtilisateurRepository utilisateurRepository;
        private PosteRepository posteRepository;
        private DepartementRepository departementRepository;
        private MotifDemandeService motifDemandeService;
        private HistoriqueDemandeRecrutementRepository historiqueRepository;
        private DemandeRecrutementService demandeRecrutementService;
        private GrilleSalarialRepository grilleSalarialRepository;

        @ModelAttribute("managers")
        public List<Utilisateur> populateManagers() {
                return utilisateurRepository.findByRole_LibelleIgnoreCase("manager");
        }

        @ModelAttribute("postes")
        public List<Poste> populatePostes() {
                return posteRepository.findAll();
        }

        @ModelAttribute("motifs")
        public List<MotifDemande> populateMotifs() {
                return motifDemandeService.getAll();
        }

        // =================================================================
        // MÉTHODES PRIVÉES UTILITAIRES
        // =================================================================

        private DemandeRecrutementDTO toDto(DemandeRecrutement demande) {
                DemandeRecrutementDTO dto = new DemandeRecrutementDTO();
                dto.setIdDemandeRecrutement(demande.getIdDemandeRecrutement());
                dto.setIdManager(demande.getManager().getIdUtilisateur());
                dto.setIdPoste(demande.getPoste().getIdPoste());
                dto.setDateSouhaitee(demande.getDateSouhaitee());
                dto.setEffectif(demande.getEffectif());
                dto.setMotif(demande.getMotif());
                dto.setEtat(demande.getEtatDemande().getIntitule());
                return dto;
        }

        private boolean canValidate(String etat, String role) {
                return ("cree".equals(etat) && "rh".equals(role)) ||
                                ("validerh".equals(etat) && "finance".equals(role)) ||
                                ("validefinance".equals(etat) && "dg".equals(role)) || "refuse".equals(etat);
        }

        // =================================================================
        // ROUTES
        // =================================================================

        @GetMapping
        public String index(@ModelAttribute("filter") DemandeRecrutementFilter filter,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {

                Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100),
                                Sort.by("dateCreation").descending());
                Page<DemandeRecrutementLib> demandesPage = demandeRepository.findAllLib(filter, pageable);

                model.addAttribute("demandes", demandesPage);
                model.addAttribute("departements", departementRepository.findAll());
                model.addAttribute("managers", utilisateurRepository.findAll()); // Note: écrase temporairement
                                                                                 // l'attribut global si besoin

                return "demande/index";
        }

        @GetMapping("/create")
        public String create(@ModelAttribute("demandeRecrutementForm") DemandeRecrutementDTO form, Model model) {
                model.addAttribute("demandeRecrutementForm",
                                form != null && form.getIdDemandeRecrutement() != null ? form
                                                : new DemandeRecrutementDTO());
                return "demande/form";
        }

        @PostMapping("/create")
        public String store(@ModelAttribute("demandeRecrutementForm") DemandeRecrutementDTO data,
                        RedirectAttributes redirectAttributes) {
                try {
                        DemandeRecrutement saved = demandeRecrutementService.createDemande(data);
                        redirectAttributes.addFlashAttribute("successMessage", "Demande créée avec succès.");
                        return "redirect:/demandes/" + saved.getIdDemandeRecrutement();
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                        redirectAttributes.addFlashAttribute("demandeRecrutementForm", data);
                        return "redirect:/demandes/create";
                }
        }

        @GetMapping("/update/{id}")
        public String edit(@PathVariable Integer id,
                        @ModelAttribute("demandeRecrutementForm") DemandeRecrutementDTO flashForm,
                        Model model) {

                model.addAttribute("isBrouillon", true);

                if (flashForm != null && flashForm.getIdDemandeRecrutement() != null) {
                        model.addAttribute("demandeRecrutementForm", flashForm);
                        return "demande/form";
                }

                DemandeRecrutement demande = demandeRecrutementService.getById(id);
                if (!"Brouillon".equalsIgnoreCase(demande.getEtatDemande().getIntitule())) {
                        return "redirect:/demandes/" + id;
                }

                model.addAttribute("demandeRecrutementForm", toDto(demande));
                return "demande/form";
        }

        @PostMapping("/update")
        public String update(@ModelAttribute("demandeRecrutementForm") DemandeRecrutementDTO data,
                        @RequestParam(value = "action", defaultValue = "save") String action,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        RedirectAttributes redirectAttributes) {
                try {
                        DemandeRecrutement updated = demandeRecrutementService.updateDemande(data,
                                        userDetails.getUtilisateur(), action);
                        redirectAttributes.addFlashAttribute("successMessage", "Demande mise à jour avec succès.");
                        return "redirect:/demandes/" + updated.getIdDemandeRecrutement();
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                        redirectAttributes.addFlashAttribute("demandeRecrutementForm", data);
                        return "redirect:/demandes/update/" + data.getIdDemandeRecrutement();
                }
        }

        @GetMapping("/{id}")
        public String show(@PathVariable Integer id, Model model,
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                DemandeRecrutement demande = demandeRecrutementService.getById(id);
                List<HistoriqueDemandeRecrutement> historique = historiqueRepository.getByIdDemandeRecrutement(id);

                String role = userDetails.getUtilisateur().getRole().getLibelle().toLowerCase();
                String etat = demande.getEtatDemande().getIntitule().toLowerCase();

                model.addAttribute("demande", demande);
                model.addAttribute("historique", historique);
                model.addAttribute("peutValider", canValidate(etat, role));

                return "demande/show";
        }

        @PostMapping("/update/{id}")
        public String validerDemande(@PathVariable Integer id,
                        @RequestParam String remarque,
                        @RequestParam String action,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        RedirectAttributes redirectAttributes) {
                try {
                        demandeRecrutementService.updateStateDemande(id, userDetails.getUtilisateur(), remarque,
                                        action);
                        redirectAttributes.addFlashAttribute("successMessage",
                                        "L'action a été enregistrée avec succès.");
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                }
                return "redirect:/demandes/" + id;
        }

        @GetMapping("/validations")
        public String getValidationPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
                String role = userDetails.getUtilisateur().getRole().getLibelle().toUpperCase();

                String etatRequis = switch (role) {
                        case "RH" -> "Cree";
                        case "FINANCE" -> "ValideRH";
                        case "DG" -> "ValideFinance";
                        default -> null;
                };

                List<DemandeRecrutement> demandes = (etatRequis != null)
                                ? demandeRepository
                                                .findByEtatDemandeIntituleIgnoreCaseOrderByDateCreationDesc(etatRequis)
                                : List.of();

                // On prépare une Map pour stocker les grilles salariales (uniquement pour la
                // finance)
                java.util.Map<Integer, GrilleSalarial> grilles = new java.util.HashMap<>();

                if ("FINANCE".equals(role)) {
                        for (DemandeRecrutement d : demandes) {
                                grilleSalarialRepository.findFirstByPosteIdPosteAndDepartementIdDepartement(
                                                d.getPoste().getIdPoste(),
                                                d.getPoste().getDepartement().getIdDepartement())
                                                .ifPresent(grille -> grilles.put(d.getIdDemandeRecrutement(), grille));
                        }
                }

                model.addAttribute("demandes", demandes);
                model.addAttribute("role", role);
                model.addAttribute("grilles", grilles);

                return "demande/validation";
        }
}