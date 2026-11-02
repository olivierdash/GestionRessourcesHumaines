package com.example.sirh.controller;

import com.example.sirh.config.CustomUserDetails;
import com.example.sirh.dto.DemandeRecrutementDTO;
import com.example.sirh.dto.DemandeRecrutementLib;
import com.example.sirh.dto.ValidationPageData;
import com.example.sirh.model.*;
import com.example.sirh.wrapper.DemandeRecrutementFilter;
import com.example.sirh.service.*;

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

    // On n'injecte plus que des services
    private DemandeRecrutementService demandeRecrutementService;
    private UtilisateurService utilisateurService;
    private PosteService posteService;
    private DepartementService departementService;
    private MotifDemandeService motifDemandeService;
    private HistoriqueDemandeRecrutementService historiqueService;

    @ModelAttribute("managers")
    public List<Utilisateur> populateManagers() {
        return utilisateurService.getManagers();
    }

    @ModelAttribute("postes")
    public List<Poste> populatePostes() {
        return posteService.getAllPostes();
    }

    @ModelAttribute("motifs")
    public List<MotifDemande> populateMotifs() {
        return motifDemandeService.getAll();
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
        
        // Appel au service
        Page<DemandeRecrutementLib> demandesPage = demandeRecrutementService.findAllLib(filter, pageable);

        model.addAttribute("demandes", demandesPage);
        model.addAttribute("departements", departementService.getAllDepartements());
        model.addAttribute("managers", utilisateurService.getAllUtilisateurs());

        return "demande/index";
    }

    @GetMapping("/create")
    public String create(@ModelAttribute("demandeRecrutementForm") DemandeRecrutementDTO form, Model model) {
        model.addAttribute("demandeRecrutementForm",
                form != null && form.getIdDemandeRecrutement() != null ? form : new DemandeRecrutementDTO());
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

        DemandeRecrutementDTO dto = demandeRecrutementService.getDtoForEdit(id);
        model.addAttribute("demandeRecrutementForm", dto);
        return "demande/form";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("demandeRecrutementForm") DemandeRecrutementDTO data,
                         @RequestParam(value = "action", defaultValue = "save") String action,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            DemandeRecrutement updated = demandeRecrutementService.updateDemande(data, userDetails.getUtilisateur(), action);
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
        
        // Appel au service d'historique au lieu du repository
        List<HistoriqueDemandeRecrutement> historique = historiqueService.getByDemandeId(id);

        String role = userDetails.getUtilisateur().getRole().getLibelle().toLowerCase();
        String etat = demande.getEtatDemande().getIntitule().toLowerCase();

        model.addAttribute("demande", demande);
        model.addAttribute("historique", historique);
        model.addAttribute("peutValider", demandeRecrutementService.canValidate(etat, role));

        return "demande/show";
    }

    @PostMapping("/update/{id}")
    public String validerDemande(@PathVariable Integer id,
                                 @RequestParam String remarque,
                                 @RequestParam String action,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            demandeRecrutementService.updateStateDemande(id, userDetails.getUtilisateur(), remarque, action);
            redirectAttributes.addFlashAttribute("successMessage", "L'action a été enregistrée avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/demandes/" + id;
    }

    @GetMapping("/validations")
    public String getValidationPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String role = userDetails.getUtilisateur().getRole().getLibelle().toUpperCase();
        
        // On prépare tout dans le service
        ValidationPageData data = demandeRecrutementService.getValidationPageData(role);

        model.addAttribute("demandes", data.getDemandes());
        model.addAttribute("role", role);
        model.addAttribute("grilles", data.getGrilles());

        return "demande/validation";
    }
}