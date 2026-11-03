package com.example.sirh.service;

import com.example.sirh.model.Affectation;
import com.example.sirh.repository.AffectationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AffectationService {

    private final AffectationRepository affectationRepository;

    @Transactional
    public Affectation save(Affectation affectation) {
        return affectationRepository.save(affectation);
    }

    @Transactional(readOnly = true)
    public List<Affectation> findByEmploye(Integer idEmploye) {
        return affectationRepository.findByEmployeIdEmployer(idEmploye);
    }
}