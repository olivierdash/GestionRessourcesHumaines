package com.example.sirh.service;

import com.example.sirh.model.TypeDecompte;
import com.example.sirh.repository.TypeDecompteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeDecompteService {

    private final TypeDecompteRepository repository;

    @Transactional(readOnly = true)
    public List<TypeDecompte> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public TypeDecompte getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type de décompte introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public TypeDecompte getByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type de décompte introuvable : " + code));
    }
}