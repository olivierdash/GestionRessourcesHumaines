package com.example.sirh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sirh.dto.DroitCongeForm;
import com.example.sirh.exception.ValidationMetierException;
import com.example.sirh.model.DroitConge;
import com.example.sirh.model.enums.ModeAcquisitionDroit;
import com.example.sirh.service.DroitCongeService;
import com.example.sirh.service.TypeCongeService;
import com.example.sirh.service.TypeContratService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/conges/droits")
@RequiredArgsConstructor
public class DroitCongeController {

    private final DroitCongeService droitCongeService;
    private final TypeCongeService typeCongeService;
    private final TypeContratService typeContratService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("droits", droitCongeService.lister());
        return "conges/droits/index";
    }

    @GetMapping("/nouveau")
    public String nouveau(@ModelAttribute("droit") DroitCongeForm form, Model model) {
        ajouterReferentiels(model);
        return "conges/droits/form";
    }

    @GetMapping("/{id}/edit")
    public String modifier(@PathVariable Integer id, Model model) {
        model.addAttribute("droit", versForm(droitCongeService.getById(id)));
        ajouterReferentiels(model);
        return "conges/droits/form";
    }

    @PostMapping
    public String enregistrer(@ModelAttribute("droit") DroitCongeForm form,
                              Model model, RedirectAttributes redirect) {
        try {
            droitCongeService.enregistrer(form);
            redirect.addFlashAttribute("succes", "Règle de droit enregistrée.");
            return "redirect:/conges/droits";
        } catch (ValidationMetierException e) {
            model.addAttribute("errors", e.getErrors());
            ajouterReferentiels(model);
            return "conges/droits/form";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            ajouterReferentiels(model);
            return "conges/droits/form";
        }
    }

    @PostMapping("/{id}/basculer")
    public String basculer(@PathVariable Integer id, RedirectAttributes redirect) {
        droitCongeService.basculerActif(id);
        redirect.addFlashAttribute("succes", "Statut de la règle modifié.");
        return "redirect:/conges/droits";
    }

    private void ajouterReferentiels(Model model) {
        model.addAttribute("types", typeCongeService.listerCongesAvecSoldeActifs());
        model.addAttribute("contrats", typeContratService.getAllTypes());
        model.addAttribute("modes", ModeAcquisitionDroit.values());
    }

    private DroitCongeForm versForm(DroitConge droit) {
        DroitCongeForm form = new DroitCongeForm();
        form.setId(droit.getId());
        form.setIdTypeConge(droit.getTypeConge() == null ? null : droit.getTypeConge().getId());
        form.setIdTypeContrat(droit.getTypeContrat() == null
                ? null : droit.getTypeContrat().getId());
        form.setAncienneteMinMois(droit.getAncienneteMinMois());
        form.setAncienneteMaxMois(droit.getAncienneteMaxMois());
        form.setModeAcquisition(droit.getModeAcquisition() == null
                ? "ANNUEL" : droit.getModeAcquisition().name());
        form.setActif(droit.getActif());
        form.setDescription(droit.getDescription());
        return form;
    }
}
