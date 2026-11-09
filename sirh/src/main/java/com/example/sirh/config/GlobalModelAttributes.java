package com.example.sirh.config;

import com.example.sirh.dto.BreadcrumbItem;
import com.example.sirh.service.BreadcrumbService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final BreadcrumbService breadcrumbService;

    @ModelAttribute("breadcrumb")
    public List<BreadcrumbItem> breadcrumb(HttpSession session) {
        return breadcrumbService.get(session);
    }

    @ModelAttribute("_csrf")
    public CsrfToken csrfToken(CsrfToken token) {
        return token;
    }
}