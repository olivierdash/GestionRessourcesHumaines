package com.example.sirh.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    // On injecte l'interceptor (qui est un @Component)
    private final BreadcrumbInterceptor breadcrumbInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(breadcrumbInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/**",        // On exclut les appels API REST
                        "/css/**",        // On exclut les ressources statiques
                        "/js/**",
                        "/images/**",
                        "/fonts/**"
                );
    }
}