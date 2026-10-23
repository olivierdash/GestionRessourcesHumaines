package com.example.sirh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Renvoie le fichier templates/login.html
    }
}