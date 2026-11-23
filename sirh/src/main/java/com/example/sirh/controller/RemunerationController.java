package com.example.sirh.controller;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sirh.dto.RemunerationDto;
import com.example.sirh.service.BulletinPaieService;
import com.example.sirh.service.EmployeService;
import com.example.sirh.service.RemunerationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/remuneration")
@RequiredArgsConstructor
public class RemunerationController {

    private final RemunerationService remunerationService;
    private final BulletinPaieService bulletinPaieService;
    private final EmployeService employeService;

    @GetMapping("/{employeId}")
    public String detail(@PathVariable Integer employeId,
                         @RequestParam(required = false) String mois,
                         Model model) {
        YearMonth periode = periode(mois);
        model.addAttribute("employe", employeService.getById(employeId));
        model.addAttribute("remuneration", remunerationService.calculer(employeId, periode));
        model.addAttribute("periode", periode);
        model.addAttribute("moisValue", periode.toString());
        return "remuneration/detail";
    }

    @GetMapping("/{employeId}/bulletin.pdf")
    public ResponseEntity<byte[]> bulletin(@PathVariable Integer employeId,
                                           @RequestParam(required = false) String mois) {
        RemunerationDto remuneration = remunerationService.calculer(employeId, periode(mois));
        byte[] pdf = bulletinPaieService.generer(remuneration);
        String nomFichier = "bulletin-" + remuneration.getMatricule() + "-"
                + remuneration.getPeriode() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + nomFichier + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private YearMonth periode(String mois) {
        if (mois == null || mois.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(mois);
        } catch (DateTimeParseException e) {
            return YearMonth.now();
        }
    }
}
