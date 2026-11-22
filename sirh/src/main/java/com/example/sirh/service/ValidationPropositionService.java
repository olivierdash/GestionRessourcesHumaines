package com.example.sirh.service;

import com.example.sirh.model.ValidationProposition;
import com.example.sirh.repository.ValidationPropositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationPropositionService {

    private final ValidationPropositionRepository validationPropositionRepository;

    @Transactional
    public ValidationProposition save(ValidationProposition validation) {
        return validationPropositionRepository.save(validation);
    }

    @Transactional(readOnly = true)
    public List<ValidationProposition> findByProposition(Integer idProposition) {
        return validationPropositionRepository
                .findByPropositionIdPropositionOrderByIdDesc(idProposition);
    }
}