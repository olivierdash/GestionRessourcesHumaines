package com.example.sirh.controller.api.v1;

import com.example.sirh.dto.CandidatLoginChangeDTO;
import com.example.sirh.dto.CandidatPublicDto;
import com.example.sirh.dto.LoginChangeDTO;
import com.example.sirh.dto.LoginRequestDto;
import com.example.sirh.dto.RegisterRequestDto;
import com.example.sirh.model.Candidat;
import com.example.sirh.service.CandidatService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3010")
public class PublicAuthController {

    private final CandidatService candidatService;

    @PostMapping("/login")
    public ResponseEntity<CandidatPublicDto> login(@RequestBody LoginRequestDto request) {
        Candidat candidat = candidatService.authenticate(
                request.getEmail(), request.getPassword());
        return ResponseEntity.ok(toDto(candidat));
    }

    @PostMapping("/register")
    public ResponseEntity<CandidatPublicDto> register(@RequestBody RegisterRequestDto request) {
        Candidat candidat = new Candidat();
        candidat.setEmail(request.getEmail());
        candidat.setNom(request.getNom());
        candidat.setNom(request.getPrenom());
        candidat.setMotDePasse(request.getPassword());

        Candidat cdt = candidatService.register(candidat);
        return ResponseEntity.ok(toDto(cdt));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody CandidatLoginChangeDTO request) {
        candidatService.resetMotDePasse(request);
        return ResponseEntity.accepted().body(Map.of("message", "Mot de passe réinitialisé avec succès."));
    }

    private CandidatPublicDto toDto(Candidat c) {
        return CandidatPublicDto.builder()
                .id(c.getIdCandidat())
                .prenom(c.getPrenom())
                .nom(c.getNom())
                .email(c.getEmail())
                .build();
    }
}