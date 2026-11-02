package com.example.sirh.service;

import com.example.sirh.model.TypeContrat;
import com.example.sirh.repository.TypeContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeContratService {

    private final TypeContractRepository typeContratRepository;

    public List<TypeContrat> getAllTypes() {
        return typeContratRepository.findAll();
    }
}