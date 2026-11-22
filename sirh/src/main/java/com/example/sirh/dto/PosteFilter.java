package com.example.sirh.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosteFilter {

    private String q;
    private Integer idDepartement;
    private Integer idLieu;

    public void setQ(String q) {
        this.q = (q == null || q.isBlank()) ? null : q.trim();
    }
}