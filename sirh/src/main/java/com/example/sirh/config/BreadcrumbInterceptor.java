package com.example.sirh.config;

import com.example.sirh.service.BreadcrumbService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class BreadcrumbInterceptor implements HandlerInterceptor {

    private final BreadcrumbService breadcrumbService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. On récupère l'URI (ex: /demandes/1)
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.replace(contextPath, "");

        // 2. On génère le breadcrumb par défaut
        var defaultBreadcrumb = breadcrumbService.generateFromUri(path);
        
        // 3. On le place dans la session (il écrasera l'ancien)
        breadcrumbService.set(request.getSession(), defaultBreadcrumb);

        return true; 
    }
}