package com.example.sirh.dto;

public record BreadcrumbItem(String label, String url) {
    // url == null ou vide => élément non cliquable (page courante)
}