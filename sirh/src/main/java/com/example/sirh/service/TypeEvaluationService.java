package com.example.sirh.service;

import com.example.sirh.model.TypeEvaluation;
import com.example.sirh.repository.TypeEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeEvaluationService {

    private final TypeEvaluationRepository typeEvaluationRepository;

    @Transactional(readOnly = true)
    public List<TypeEvaluation> getAllTypes() {
        return typeEvaluationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TypeEvaluation getById(Integer id) {
        return typeEvaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type d'évaluation introuvable : " + id));
    }
}