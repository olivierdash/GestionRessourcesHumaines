package com.example.sirh.controller.api.v1;

import com.example.sirh.dto.CandidatPublicDto;
import com.example.sirh.dto.LoginRequestDto;
import com.example.sirh.dto.RegisterRequestDto;
import com.example.sirh.model.Candidat;
import com.example.sirh.service.CandidatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
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
        Candidat candidat = candidatService.register(
                request.getPrenom(), request.getNom(),
                request.getEmail(), request.getPassword());
        return ResponseEntity.ok(toDto(candidat));
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