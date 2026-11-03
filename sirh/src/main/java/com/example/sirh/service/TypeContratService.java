package com.example.sirh.service;

import com.example.sirh.model.TypeContrat;
import com.example.sirh.repository.TypeContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeContratService {

    private final TypeContractRepository typeContratRepository;

    @Transactional(readOnly = true)
    public List<TypeContrat> getAllTypes() {
        return typeContratRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TypeContrat getById(Integer id) {
        return typeContratRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type de contrat introuvable : " + id));
    }
}