package com.example.sirh.service;

import com.example.sirh.model.TypeEvaluation;
import com.example.sirh.repository.TypeEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeEvaluationService {

    private final TypeEvaluationRepository typeEvaluationRepository;

    public List<TypeEvaluation> getAllTypes() {
        return typeEvaluationRepository.findAll();
    }
}