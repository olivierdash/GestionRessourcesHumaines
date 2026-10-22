package com.example.sirh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/utilisateur/login")
    public String login() {
        return "login";
    }

    @GetMapping("/utilisateur/dashboard")
    public String dashboard() {
        return "pages/dashboard";
    }
}
